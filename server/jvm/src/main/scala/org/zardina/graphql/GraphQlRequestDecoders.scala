package org.zardina.graphql

import GraphQlQueryDecoders._
import com.twitter.io.Buf
import com.twitter.util.{ Return, Throw, Try }
import io.circe.Decoder
import io.finch.Decode
import io.circe.parser._

trait GraphQlRequestDecoders {
  type JsonCleaner = (Buf) => Buf

  /**
   * Decodes a payload, where the data to be decoded sits as an object inside a top level `data` field of the
   * request body. For example: `{ "data" : { ... } }`.
   */
  final def decodeDataJson[A](d: Decoder[A], c: JsonCleaner = identity): Decode.Json[A] =
    Decode.json((payload, _) => decodePayload(c(payload), dataFieldObjectDecoder(d)))

  /**
   * Decodes a payload, where the data to be decoded is an object at the root level of the request body. For
   * example: `{ ... }`.
   */
  final def decodeRootJson[A](d: Decoder[A], c: JsonCleaner = identity): Decode.Json[A] =
    Decode.json((payload, _) => decodePayload(c(payload), rootObjectDecoder(d)))

  final def bufToString(buf: Buf): String = {
    val output = new Array[Byte](buf.length)
    buf.write(output, 0)
    new String(output)
  }

  private def decodePayload[A](payload: Buf, decoder: Decoder[A]): Try[A] = {
    val decodedPayload = parse(bufToString(payload)).flatMap(decoder.decodeJson)
    decodedPayload.fold(
      error => Throw(new IllegalArgumentException(s"Unable to decode JSON payload: ${error.getMessage}", error)),
      value => Return(value))
  }

  private def dataFieldObjectDecoder[A](implicit d: Decoder[A]): Decoder[A] =
    Decoder.instance(c => c.downField("data").as[A](d))

  private def rootObjectDecoder[A](implicit d: Decoder[A]): Decoder[A] = Decoder.instance(c => c.as[A](d))

  implicit val graphQlQueryDecode: Decode.Json[GraphQlQuery] =
    decodeRootJson[GraphQlQuery](queryDecoder, cleanJson)
}

object GraphQlRequestDecoders extends GraphQlRequestDecoders
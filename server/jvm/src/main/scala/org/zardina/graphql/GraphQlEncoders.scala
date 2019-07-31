package org.zardina.graphql

import com.twitter.io.Buf
import io.circe.{ Encoder, Json, Printer }
import io.finch.Encode

trait GraphQlEncoders {
  private val printer = Printer.noSpaces.copy(dropNullValues = true)
  val graphQlResultEncoder: Encoder[Json] = Encoder.instance[Json](r => r)
  final def rootJsonEncode[A](implicit encoder: Encoder[A]): Encode.Json[A] =
    Encode.json { (a, _) =>
      Buf.ByteBuffer.Owned(printer.prettyByteBuffer((encoder.apply(a))))
    }
  implicit val graphQlResultEncode: Encode.Json[Json] = rootJsonEncode[Json](graphQlResultEncoder)
}

object GraphQlEncoders extends GraphQlEncoders
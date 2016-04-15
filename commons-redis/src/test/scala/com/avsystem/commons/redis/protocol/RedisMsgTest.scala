package com.avsystem.commons
package redis.protocol

import akka.util.ByteString
import com.avsystem.commons.redis.protocol.RedisMsgScalacheck._
import org.scalacheck.Gen
import org.scalatest.FunSuite
import org.scalatest.prop.PropertyChecks

import scala.collection.immutable.VectorBuilder

/**
  * Author: ghik
  * Created: 01/04/16.
  */
class RedisMsgTest extends FunSuite with PropertyChecks {
  def splitAtIndices(repr: ByteString, indices: Seq[Int]): Seq[ByteString] =
    (indices :+ repr.length).foldLeft((0, Vector.empty[ByteString])) {
      case ((prevIdx, acc), nextIdx) => (nextIdx, acc :+ repr.slice(prevIdx, nextIdx))
    }._2

  test("encoded and then decoded messages should be equal to the original messages") {
    val gen = for {
      redisMsgs <- Gen.buildableOf[Seq[RedisMsg], RedisMsg](redisMsgGen)
      splitPoints <- Gen.buildableOf[Seq[Double], Double](Gen.choose(0.0, 1.0))
    } yield (redisMsgs, splitPoints)

    forAll(gen) { case (redisMsgs, splitPoints) =>
      val repr = RedisMsg.encode(redisMsgs)
      val splitIndices = splitPoints.map(sp => (sp * (repr.size - 1)).toInt).toSet.toVector.sorted
      val encodedParts = splitAtIndices(repr, splitIndices)
      val decoded = new VectorBuilder[RedisMsg]
      val decoder = new RedisMsg.Decoder(decoded += _)
      encodedParts.foreach(decoder.decodeMore)
      val decodedMsgs = decoded.result()
      assert(decodedMsgs == redisMsgs)
    }
  }

  test("simple string encode") {
    assert(RedisMsg.encode(SimpleStringMsg("asdf")).utf8String == "+asdf\r\n")
  }

  test("error encode") {
    assert(RedisMsg.encode(ErrorMsg("asdf")).utf8String == "-asdf\r\n")
  }

  test("bulk string encode") {
    assert(RedisMsg.encode(BulkStringMsg(ByteString("srsly"))).utf8String == "$5\r\nsrsly\r\n")
  }

  test("null bulk string encode") {
    assert(RedisMsg.encode(NullBulkStringMsg).utf8String == "$-1\r\n")
  }

  test("array encode") {
    assert(RedisMsg.encode(ArrayMsg(Vector(IntegerMsg(42), IntegerMsg(43)))).utf8String == "*2\r\n:42\r\n:43\r\n")
  }

  test("null array encode") {
    assert(RedisMsg.encode(NullArrayMsg).utf8String == "*-1\r\n")
  }

  test("integer encode") {
    assert(RedisMsg.encode(IntegerMsg(-1)).utf8String == ":-1\r\n")
  }
}

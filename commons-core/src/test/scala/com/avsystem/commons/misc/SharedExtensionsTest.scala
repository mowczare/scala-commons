package com.avsystem.commons
package misc

import org.scalatest.{FunSuite, Matchers}

class SharedExtensionsTest extends FunSuite with Matchers {
  test("mkMap") {
    List.range(0, 3).mkMap(identity, _.toString) shouldEqual
      Map(0 -> "0", 1 -> "1", 2 -> "2")
  }

  test("groupToMap") {
    List.range(0, 10).groupToMap(_ % 3, _.toString) shouldEqual
      Map(0 -> List("0", "3", "6", "9"), 1 -> List("1", "4", "7"), 2 -> List("2", "5", "8"))
  }

  test("maxOpt") {
    List.range(0, 10).maxOpt shouldEqual Opt(9)
    List.empty[Int].maxOpt shouldEqual Opt.Empty
  }

  test("Future.eval") {
    val ex = new Exception
    assert(Future.eval(42).value.contains(Success(42)))
    assert(Future.eval(throw ex).value.contains(Failure(ex)))
  }

  test("Future.transformTry") {
    import com.avsystem.commons.concurrent.RunNowEC.Implicits._
    val ex = new Exception
    assert(Future.successful(42).transformTry(t => Success(t.get - 1)).value.contains(Success(41)))
    assert(Future.successful(42).transformTry(_ => Failure(ex)).value.contains(Failure(ex)))
    assert(Future.failed[Int](ex).transformTry(t => Success(t.failed.get)).value.contains(Success(ex)))
    assert(Future.failed[Int](ex).transformTry(_ => Failure(ex)).value.contains(Failure(ex)))
  }

  test("Future.transformWith") {
    import com.avsystem.commons.concurrent.RunNowEC.Implicits._
    val ex = new Exception
    assert(Future.successful(42).transformWith(t => Future.successful(t.get - 1)).value.contains(Success(41)))
    assert(Future.successful(42).transformWith(_ => Future.failed(ex)).value.contains(Failure(ex)))
    assert(Future.failed[Int](ex).transformWith(t => Future.successful(t.failed.get)).value.contains(Success(ex)))
    assert(Future.failed[Int](ex).transformWith(_ => Future.failed(ex)).value.contains(Failure(ex)))
  }

  test("Iterator.untilEmpty") {
    var i = 0
    assert(Iterator.untilEmpty {
      i += 1
      i.opt.filter(_ <= 5)
    }.toList == List(1, 2, 3, 4, 5))
  }

  test("Iterator.iterateUntilEmpty") {
    assert(Iterator.iterateUntilEmpty(Opt.empty[Int])(i => (i + 1).opt).toList == Nil)
    assert(Iterator.iterateUntilEmpty(1.opt)(i => (i + 1).opt.filter(_ <= 5)).toList == List(1, 2, 3, 4, 5))
  }

  test("IteratorOps.collectWhileDefined") {
    assert(Iterator(1, 2, 3, 2, 1).collectWhileDefined({ case n if n < 3 => n * 2 }).toList == List(2, 4))
    assert(Iterator[Int]().collectWhileDefined({ case n if n < 3 => n * 2 }).toList == Nil)
    assert(Iterator(1, 2, 3, 2, 1).collectWhileDefined({ case n if n > 0 => n * 2 }).toList == List(2, 4, 6, 4, 2))
  }

  test("IteratorOps.distinctBy") {
    assert(
      Iterator("ab", "ba", "ac", "cd", "ad", "bd", "be", "fu").distinctBy(_.charAt(0)).toList ==
        List("ab", "ba", "cd", "fu")
    )
  }

  test("uncheckedMatch") {
    val res = Option(42) uncheckedMatch {
      case Some(int) => int
    }
    assert(res == 42)

    assertThrows[MatchError] {
      Option.empty[Int] uncheckedMatch {
        case Some(int) => int
      }
    }
  }

  test("Ordering.orElse") {
    case class CC(f: Int, s: Int)

    val o = Ordering.by((_: CC).f).orElseBy(_.s)

    assert(o.compare(CC(0, 1), CC(1, 0)) < 0)
    assert(o.compare(CC(1, 0), CC(0, 1)) > 0)
    assert(o.compare(CC(0, 0), CC(0, 1)) < 0)
    assert(o.compare(CC(0, 1), CC(0, 0)) > 0)
    assert(o.compare(CC(0, 0), CC(0, 0)) == 0)
  }
}

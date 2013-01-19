package test.roundeights.attempt

import org.specs2.mutable._
import org.specs2.mock._

import com.roundeights.attempt._
import scala.concurrent._
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration._

object OrDoTest extends Specification with Mockito {

    /** Blocks while waiting for the given future */
    def await[T] ( reader: Future[T] ): Unit
        = Await.ready( reader, Duration(5, "second") )


    "An OrDo given an Option" should {

        "Not run the code when the Option is a 'Some'" in {
            val onFailure = mock[Runnable]
            val opt = Some("Value")
            ( opt :: OrDo( onFailure.run ) ) must_== opt
            there was no(onFailure).run()
        }

        "Run the code when the Option is a 'None'" in {
            val onFailure = mock[Runnable]
            val opt = None
            ( opt :: OrDo( onFailure.run ) ) must_== opt
            there was one(onFailure).run()
        }

    }

    "An OrDo given an Either" should {

        "Not run the code when the Either is a 'Right'" in {
            val onFailure = mock[Runnable]
            val eigher = Right("Value")
            ( eigher :: OrDo( onFailure.run ) ) must_== eigher
            there was no(onFailure).run()
        }

        "Run the code when the Either is a 'Left'" in {
            val onFailure = mock[Runnable]
            val eigher = Left("Value")
            ( eigher :: OrDo( onFailure.run ) ) must_== eigher
            there was one(onFailure).run()
        }

    }

    "An OrDo given a Future" should {

        "Not run the code when the Future is successful" in {
            val onFailure = mock[Runnable]
            val future = Future.successful("Value")
            ( future :: OrDo( onFailure.run ) ) must_== future
            await( future )
            there was no(onFailure).run()
        }

        "Run the code when the Future is failed" in {
            val onFailure = mock[Runnable]
            val future: Future[String] = Future.failed( new Exception )
            ( future :: OrDo( onFailure.run ) ) must_== future
            await( future )
            there was one(onFailure).run()
        }

    }

}

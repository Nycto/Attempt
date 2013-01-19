package test.roundeights.attempt

import org.specs2.mutable._
import org.specs2.mock._

import com.roundeights.attempt._
import scala.concurrent._
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration._

object OnFailTest extends Specification with Mockito {

    /** Blocks while waiting for the given future */
    def await[T] ( reader: Future[T] ): Unit
        = Await.ready( reader, Duration(5, "second") )


    "An OnFail given a Boolean" should {

        "Not run the code when the value is true" in {
            val onFailure = mock[Runnable]
            ( true :: OnFail( onFailure.run ) ) must_== Some(true)
            there was no(onFailure).run()
        }

        "Run the code when the value is false" in {
            val onFailure = mock[Runnable]
            ( false :: OnFail( onFailure.run ) ) must_== None
            there was one(onFailure).run()
        }

    }

    "An OnFail given an Option" should {

        "Not run the code when the Option is a 'Some'" in {
            val onFailure = mock[Runnable]
            val opt = Some("Value")
            ( opt :: OnFail( onFailure.run ) ) must_== opt
            there was no(onFailure).run()
        }

        "Run the code when the Option is a 'None'" in {
            val onFailure = mock[Runnable]
            val opt = None
            ( opt :: OnFail( onFailure.run ) ) must_== opt
            there was one(onFailure).run()
        }

    }

    "An OnFail given an Either" should {

        "Not run the code when the Either is a 'Right'" in {
            val onFailure = mock[Runnable]
            ( Right("Value") :: OnFail( onFailure.run ) ) must_== Some("Value")
            there was no(onFailure).run()
        }

        "Run the code when the Either is a 'Left'" in {
            val onFailure = mock[Runnable]
            ( Left("Value") :: OnFail( onFailure.run ) ) must_== None
            there was one(onFailure).run()
        }

    }

    "An OnFail given a Future" should {

        "Not run the code when the Future is successful" in {
            val onFailure = mock[Runnable]
            val future = Future.successful("Value")
            ( future :: OnFail( onFailure.run ) ) must_== future
            await( future )
            there was no(onFailure).run()
        }

        "Run the code when the Future is failed" in {
            val onFailure = mock[Runnable]
            val future: Future[String] = Future.failed( new Exception )
            ( future :: OnFail( onFailure.run ) ) must_== future
            await( future )
            there was one(onFailure).run()
        }

    }

    "An OnFailWith given an Either" should {

        "Not run the code when the Either is a 'Right'" in {
            val onFailure = mock[Runnable]

            val result = Right("Value") :: OnFail(
                (value: String) => onFailure.run
            )

            result must_== Some("Value")
            there was no(onFailure).run()
        }

        "Run the code when the Either is a 'Left'" in {
            val onFailure = mock[Runnable]

            val result = Left("Value") :: OnFail( (value: String) => {
                value must_== "Value"
                onFailure.run
            } )

            result must_== None
            there was one(onFailure).run()
        }

    }

    "An OnFailWith given a Future" should {

        "Not run the code when the Future is successful" in {
            val onFailure = mock[Runnable]
            val future = Future.successful("Value")

            val result = future :: OnFail( (err: Throwable) => onFailure.run )

            result must_== future

            await( future )
            there was no(onFailure).run()
        }

        "Run the code when the Future is failed" in {
            val onFailure = mock[Runnable]
            val error = new Exception("Failed Future")
            val future = Future.failed( error )

            val result = future :: OnFail( (err: Throwable) => {
                err must_== error
                onFailure.run
            } )

            result must_== future

            await( future )
            there was one(onFailure).run()

        }

    }

}


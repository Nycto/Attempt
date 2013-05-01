package com.roundeights.attempt

import org.specs2.mutable._
import org.specs2.mock._

import scala.concurrent._
import com.roundeights.attempt.FutureTester._

object OnFailTest extends Specification with Mockito {

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
            val result = ( Future.successful("Val") :: OnFail(onFailure.run) )
            await( result ) must_== "Val"
            there was no(onFailure).run()
        }

        "Run the code when the Future is failed" in {
            val error = new Exception("Expected exception")
            val onFailure = mock[Runnable]
            val result = ( Future.failed(error) :: OnFail(onFailure.run) )
            await( result.failed ) must_== error
            there was one(onFailure).run()
        }

        "Return an exception thrown by the fail handler" in {
            val error1 = new Exception("Initial Exception")
            val error2 = new Exception("Expected exception")
            val result = ( Future.failed(error1) :: OnFail( throw error2 ) )
            await( result.failed ) must_== error2
        }
    }

    "An OnFailWith given an Either" should {

        "Not run the code when the Either is a 'Right'" in {
            val onFailure = mock[Runnable]

            val result = Right("Value") :: OnFail.call(
                (value: String) => onFailure.run
            )

            result must_== Some("Value")
            there was no(onFailure).run()
        }

        "Run the code when the Either is a 'Left'" in {
            val onFailure = mock[Runnable]

            val result = Left("Value") :: OnFail.call( (value: String) => {
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

            val result = Future.successful("Value") :: OnFail.call {
                (err: Throwable) => onFailure.run
            }

            await( result ) must_== "Value"
            there was no(onFailure).run()
        }

        "Run the code when the Future is failed" in {
            val onFailure = mock[Runnable]
            val error = new Exception("Failed Future")

            val result = Future.failed(error) :: OnFail.call(
                (err: Throwable) => {
                    err must_== error
                    onFailure.run
                }
            )

            await( result.failed ) must_== error
            there was one(onFailure).run()
        }

    }

}


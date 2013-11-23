package com.roundeights.attempt

import org.specs2.mutable._
import org.specs2.mock._

import scala.concurrent._
import java.util.concurrent.Executor

object OnFailTest extends Specification with Mockito {

    /** An execution context that runs in the calling thread */
    implicit val context = ExecutionContext.fromExecutor(new Executor {
        override def execute( command: Runnable ): Unit = command.run
    })

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
            result must ===("Val").await
            there was no(onFailure).run()
        }

        "Run the code when the Future is failed" in {
            val error: Throwable = new Exception("Expected exception")
            val onFailure = mock[Runnable]
            val result = ( Future.failed(error) :: OnFail(onFailure.run) )
            result.failed must ===(error).await
            there was one(onFailure).run()
        }

        "Return an exception thrown by the fail handler" in {
            val error1: Throwable = new Exception("Initial Exception")
            val error2: Throwable = new Exception("Expected exception")
            val result = ( Future.failed(error1) :: OnFail( throw error2 ) )
            result.failed must ===(error2).await
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

            result must ===("Value").await
            there was no(onFailure).run()
        }

        "Run the code when the Future is failed" in {
            val onFailure = mock[Runnable]
            val error: Throwable = new Exception("Failed Future")

            val result = Future.failed(error) :: OnFail.call(
                (err: Throwable) => {
                    err must_== error
                    onFailure.run
                }
            )

            result.failed must ===(error).await
            there was one(onFailure).run()
        }

    }

}


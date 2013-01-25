package com.roundeights.attempt

import org.specs2.mutable._
import org.specs2.mock._

import scala.concurrent._
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration._

object TryToTest extends Specification with Mockito {

    /** Blocks while waiting for the given future */
    def await[T] ( reader: Future[T] ): Unit
        = Await.ready( reader, Duration(10, "second") )


    "A TryTo given a Boolean" should {

        "Not run the code when the value is true" in {
            val onFailure = mock[Runnable]
            TryTo( true ).onFail( onFailure.run ) must_== Some(true)
            there was no(onFailure).run()
        }

        "Run the code when the value is false" in {
            val onFailure = mock[Runnable]
            TryTo( false ).onFail( onFailure.run ) must_== None
            there was one(onFailure).run()
        }

    }

    "A TryTo given an Option" should {

        "Not run the code when the Option is a 'Some'" in {
            val onFailure = mock[Runnable]
            TryTo( Some("Val") ).onFail( onFailure.run ) must_== Some("Val")
            there was no(onFailure).run()
        }

        "Run the code when the Option is a 'None'" in {
            val onFailure = mock[Runnable]
            TryTo( None ).onFail( onFailure.run ) must_== None
            there was one(onFailure).run()
        }

    }

    "A TryTo given an Either" should {

        "Not run the code when the Either is a 'Right'" in {
            val onFailure = mock[Runnable]
            val result = TryTo( Right("Value") ).onFail( onFailure.run )
            result must_== Some("Value")
            there was no(onFailure).run()
        }

        "Run the code when the Either is a 'Left'" in {
            val onFailure = mock[Runnable]
            TryTo( Left("Value") ).onFail( onFailure.run ) must_== None
            there was one(onFailure).run()
        }

    }

    "A TryTo.except" should {

        "Not run the code when an exception is not thrown" in {
            val onFailure = mock[Runnable]

            val result = TryTo.except( "Value" ).onFail { onFailure.run }

            result must_== Some("Value")
            there was no(onFailure).run()
        }

        "Run the code when an exception is thrown" in {
            val onFailure = mock[Runnable]
            val except = new Exception

            val result = TryTo.except( throw except ).onFailMatch {
                case err: Throwable => {
                    err must_== except
                    onFailure.run
                }
            }

            result must_== None
            there was one(onFailure).run()
        }

        "Propagate the exception if the PartialFunction doesnt match it" in {
            val onFailure = mock[Runnable]

            {
                TryTo.except( throw new Exception ).onFailMatch {
                    case err: RuntimeException => onFailure.run
                }
            } must throwA[Exception]


            there was no(onFailure).run()
        }

    }

    "A TryToWith given a Future" should {

        "Not run the code when the Future is successful" in {
            val onFailure = mock[Runnable]
            val future = Future.successful("Value")

            val result = TryTo( future ).onFailMatch {
                case err: Throwable => onFailure.run
            }

            result must_== future

            await( future )
            there was no(onFailure).run()
        }

        "Run the code when the Future is failed" in {
            val onFailure = mock[Runnable]
            val error = new Exception("Failed Future")
            val future = Future.failed( error )

            val result = TryTo( future ).onFailMatch {
                case err: Throwable => {
                    err must_== error
                    onFailure.run
                }
            }

            result must_== future

            await( future )
            there was one(onFailure).run()
        }

    }

}



package com.roundeights.attempt

import org.specs2.mutable._
import org.specs2.mock._

import scala.concurrent._
import scala.concurrent.ExecutionContext.Implicits.global

object TryToTest extends Specification with Mockito {

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

            val result = TryTo( Future.successful("Value") ).onFailMatch {
                case err: Throwable => onFailure.run
            }

            result must ===("Value").await
            there was no(onFailure).run()
        }

        "Run the code when the Future is failed" in {
            val onFailure = mock[Runnable]
            val error: Throwable = new Exception("Failed Future")

            val result = TryTo( Future.failed( error ) ).onFailMatch {
                case err: Throwable => {
                    err must_== error
                    onFailure.run
                }
            }

            result.failed must ===(error).await
            there was one(onFailure).run()
        }

        "Complete with the new exception if the fail handler throws" in {
            val error1: Throwable = new Exception("Failed Future")
            val error2: Throwable = new Exception("Failed Callback")

            val result = TryTo( Future.failed( error1 ) ).onFailMatch {
                case err: Throwable => {
                    err must_== error1
                    throw error2
                }
            }

            result.failed must ===(error2).await
        }

    }

    "A TryTo.lift on a Future" should {

        "Succeed the returned future with the unwrapped value" in {
            val onFailure = mock[Runnable]

            val result = TryTo.lift( Future.successful(Some("Test")) )
                .onFail { onFailure.run }

            result must ===("Test").await
            there was no(onFailure).run()
        }

        "Fail when the result is a None" in {
            val onFailure = mock[Runnable]

            val result = TryTo.lift( Future.successful(None) )
                .onFail { onFailure.run }

            result.failed must beAnInstanceOf[Exception].await
            there was one(onFailure).run()
        }

        "Not run the fail method when the future fails" in {
            val onFailure = mock[Runnable]
            val error: Throwable = new Exception("Failed Future")

            val result = TryTo.lift( Future.failed(error) )
                .onFail { onFailure.run }

            result.failed must ===(error).await
            there was no(onFailure).run()
        }
    }

    "A TryTo.lift on an Option" should {

        "Succeed when both Options are Some" in {
            TryTo.lift( Some(Some("Test")) )
                .onFail { throw new Exception("Should not encounter") }
                .must_==( Some("Test") )
        }

        "Fail when the outer Option is a None" in {
            val onFailure = mock[Runnable]
            TryTo.lift( None ).onFail { onFailure.run } must_== None
            there was one(onFailure).run()
        }

        "Fail when the inner Option is a None" in {
            val onFailure = mock[Runnable]
            TryTo.lift( Some(None) ).onFail { onFailure.run } must_== None
            there was one(onFailure).run()
        }
    }

    "A TryTo.liftBool" should {

        "Succeed when the result is true" in {
            val onFailure = mock[Runnable]

            val result = TryTo.liftBool( Future.successful(true) )
                .onFail { onFailure.run }

            result must ===(true).await
            there was no(onFailure).run()
        }

        "Fail when the result is a false" in {
            val onFailure = mock[Runnable]

            val result = TryTo.liftBool( Future.successful(false) )
                .onFail { onFailure.run }

            result.failed must beAnInstanceOf[Throwable].await
            there was one(onFailure).run()
        }

        "Not run the fail method when the future fails" in {
            val onFailure = mock[Runnable]
            val error: Throwable = new Exception("Failed Future")

            val result = TryTo.liftBool( Future.failed(error) )
                .onFail { onFailure.run }

            result.failed must ===(error).await
            there was no(onFailure).run()
        }
    }

    "A TryTo that fails a promise" should {

        "Not touch that promise if there isn't a failure" in {
            val noChange = Promise[String]
            TryTo { Future.successful("Something") } onFailAlsoFail noChange
            noChange.success("Result")
            noChange.future must ===("Result").await
        }

        "Fail the promise if the future fails" in {
            val err: Throwable = new Exception("Expected error")
            val toFail = Promise[String]
            TryTo { Future.failed(err) } onFailAlsoFail toFail
            toFail.future.failed must ===(err).await
        }
    }
}



package test.roundeights.attempt

import org.specs2.mutable._

object AttemptTest extends Specification {

    import com.roundeights.attempt._

    "Attempts using an Option" should {

        "yield the final value if all the errors are successful" in {
            val result = for {
                a <- Attempt( Some("abc") ).onFail( "Error 1" )
                b <- Attempt( Some("xyz" + a) ).onFail( "Error 2" )
            } yield b

            result must_== Success("xyzabc", "Error 1")
        }

        "yield an intermediary error if anything is None" in {
            val result = for {
                a <- Attempt( None ).onFail( "Error 1" )
                b <- Attempt( Some("xyz" + a) ).onFail( "Error 2" )
            } yield b

            result must_== Failure("Error 1")
        }

        "not evaluate the failure condition unless it is encountered" in {
            val result = for {
                a <- Attempt( Some(1234) ).onFail( throw new Exception )
            } yield a

            result must_== Success(1234, "Err")
        }

    }

    "Attempts with if guards" should {

        "continue when an if check succeeds" in {
            val result = for {
                a <- Attempt( Some(1234) ).onFail( "Error 1" )
                if ( a == 1234 )
                b <- Attempt( Some(a + 6) ).onFail( "Error 2" )
            } yield b

            result must_== Success(1240, "Error 1")
        }

        "short circuit when an if check fails" in {
            val result = for {
                a <- Attempt( Some(1234) ).onFail( "Error 1" )
                if ( a != 1234 )
                b <- Attempt( Some(a + 6) ).onFail( "Error 2" )
            } yield b

            result must_== Failure("Error 1")
        }

        "not evaluate the failure condition unless it is encountered" in {
            val result = for {
                a <- Attempt( Some(1234) ).onFail( throw new Exception )
                if ( a == 1234 )
            } yield a

            result must_== Success(1234, "Err")
        }

    }

    "Attempts constructed with booleans" should {

        "be Successful for True" in {
            val result = for {
                a <- Attempt( true ).onFail( "Err 1" )
            } yield a

            result must_== Success(true, "Err")
        }

        "fail when evaluated to false" in {
            val result = for {
                _ <- Attempt( true ).onFail( "Err 1" )
                a <- Attempt( false ).onFail( "Error 2" )
            } yield a

            result must_== Failure("Error 2")
        }

    }

    "Attempts constructed to be always successful" should {

        "be Successful for True" in {
            val result = for {
                a <- Success( "Success!" )
            } yield a

            result must beLike {
                case Success("Success!") => ok
            }
        }

        "Throw when guarded" in {
            {
                for {
                    a <- Success( "Success!" )
                    if ( false )
                } yield a
            } must throwAn[Exception]
        }

    }

    "Using Attempt.except" should {

        "absorb exceptions" in {
            val result = for {
                a <- Attempt.except( throw new Exception ).onFail( "Error" )
            } yield a

            result must_== Failure("Error")
        }

        "yield the conditional value if there is no exception" in {
            val result = for {
                a <- Attempt.except( 123 ).onFail( "Error" )
            } yield a

            result must_== Success(123, "Error")
        }

    }

    "Implicit conversions for an Attempt" should {

        "Convert a Failure to a Left" in {
            val result: Either[String, String] = Failure[String,String]("Fail")
            result must_== Left("Fail")
        }

        "Convert a Success to a Right" in {
            val result: Either[String, String]
                = Success[String,String]("Pass", "Fail")
            result must_== Right("Pass")
        }

        "Convert a Failure to a None" in {
            val result: Option[String] = Failure[String,String]("Fail")
            result must_== None
        }

        "Convert a Success to a Right" in {
            val result: Option[String]
                = Success[String,String]("Pass", "Fail")
            result must_== Some("Pass")
        }

    }

    "The extract method" should {

        "return the value from a success" in {
            Success[Int, String]( 123, "ABC" ).extract[Int] must_== 123
        }

        "return the error from a failure" in {
            Failure[Int, String]( "ABC" ).extract[String] must_== "ABC"
        }

    }

}

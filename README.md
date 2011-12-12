Attempt
=======

Attempt is a micro-library for Scala that makes it possible to use an
`Either`-like structure in sequence comprehensions. The idea is to make the
logic for accumulating data easier to read, while providing custom error
messaging at each phase. If one step fails, all the rest are automatically
short circuited.

It's easier to see an example:

    def process ( data: Map[String, String] ): Either[String, Product] = {

        import com.roundeights.attempt.Attempt

        val response = for {

            userIdString <- Attempt(
                // Calling 'get' on a map returns an Option
                data.get("userId"),
                "Data is missing the 'userId' key"
            )

            // Attempt.except will absorb exceptions and treat them as
            // failures
            userId <- Attempt.except( userIdString.toInt, "Invalid userId" )

            // Guards are supported. They will use the error message
            // of the attempt they are attached to.
            if ( userId > 0 )

            user <- Attempt(
                // findByuserId should return an Option
                findUserByID( userId ),
                "User could not be found"
            )

            productIdString <- Attempt(
                // Calling 'get' on a map returns an Option
                data.get("productID"),
                "Data is missing the 'productID' key"
            )

            productID <- Attempt.except(
                productIdString.toInt,
                "Invalid productId"
            )

            // Another way to handle boolean conditions, but this time it
            // supports custom messaging:
            _ <- Attempt(
                productID > 0,
                "productID must be a positive integer"
            )

            product <- Attempt(
                // getPurchase should return an Option
                user.getPurchase("productID"),
                "User has not purchased that product"
            )

        } yield product

    }

License
-------

Hasher is released under the MIT License, which is pretty spiffy. You should
have received a copy of the MIT License along with this program. If not, see
<http://www.opensource.org/licenses/mit-license.php>.


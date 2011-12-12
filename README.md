Attempt
=======

Attempt is a micro-library for Scala that makes it possible to use an
`Either`-like structure in sequence comprehensions. The idea is to make the
logic for accumulating data easier to read, while providing custom error
messaging at each phase. If one step fails, all the rest are automatically
short circuited.

It's easier to see an example:

```scala

    def process ( data: Map[String, String] ): Either[String, Product] = {

        import com.roundeights.attempt.Attempt

        val response = for {

            userIdString <- Attempt(
                // Calling 'get' on a map returns an Option
                data.get("userId"),
                "Data is missing the 'userId' key"
            )

            // Attempt.except will absorb exceptions and treat them as failures
            userId <- Attempt.except( userIdString.toInt, "Invalid userId" )

            // Guards are supported. They will use the error message
            // of the Attempt that immediately precedes them.
            if ( userId > 0 )

            // findByUserId should return an Option
            user <- Attempt( findUserByID( userId ), "User could not be found" )

            productIdString <- Attempt(
                // Calling 'get' on a map returns an Option
                data.get("productId"),
                "Data is missing the 'productId' key"
            )

            productId <- Attempt.except(
                productIdString.toInt,
                "Invalid productId"
            )

            // Another way to handle boolean conditions, but this time it
            // supports custom messaging
            _ <- Attempt(
                productId > 0,
                "productId must be a positive integer"
            )

            product <- Attempt(
                // getPurchase should return an Option
                user.getPurchase( productId ),
                "User has not purchased that product"
            )

        } yield product

    }

```

License
-------

Hasher is released under the MIT License, which is pretty spiffy. You should
have received a copy of the MIT License along with this program. If not, see
<http://www.opensource.org/licenses/mit-license.php>.


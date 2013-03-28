Attempt [![Build Status](https://secure.travis-ci.org/Nycto/Attempt.png?branch=master)](http://travis-ci.org/Nycto/Attempt)
=======

Attempt is a micro-library for Scala that makes it possible to use an
`Either`-like structure in sequence comprehensions. The idea is to make the
logic for accumulating data easier to read, while providing custom error
messaging at each phase. If one step fails, all the rest are automatically
short circuited.

It's easier to see an example:

```scala

import com.roundeights.attempt.Attempt

class Process ( data: Map[String, String] ) = {

    val result: Either[String, Product]  = for {

        // Giving an Option to Attempt will call the error if it is None
        userIdString <- Attempt( data.get("userId") ).onFail {
            "Data is missing the 'userId' key"
        }

        // Attempt.except will absorb exceptions and treat them as failures
        userId <- Attempt.except( userIdString.toInt ).onFail {
            "Invalid userId"
        }

        // Guards are supported. They will use the error message
        // of the Attempt that immediately precedes them.
        if ( userId > 0 )

        // findByUserId should return an Option
        user <- Attempt( findUserByID( userId ) ).onFail {
            "User could not be found"
        }

        productIdString <- Attempt( data.get("productId") ).onFail {
            "Data is missing the 'productId' key"
        }

        productId <- Attempt.except( productIdString.toInt ).onFail {
            "Invalid productId"
        }

        // Another way to handle boolean conditions, but this time it
        // supports custom messaging
        _ <- Attempt( productId > 0 ).onFail {
            "productId must be a positive integer"
        }

        // getPurchase should return an Option
        product <- Attempt( user.getPurchase( productId ) ).onFail {
            "User has not purchased that product"
        }

    } yield product

}

```

License
-------

Hasher is released under the MIT License, which is pretty spiffy. You should
have received a copy of the MIT License along with this program. If not, see
<http://www.opensource.org/licenses/mit-license.php>.


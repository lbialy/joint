package machinespir.it.joint

import cats.effect.IO
import com.dimafeng.testcontainers.{ForAllTestContainer, PostgreSQLContainer}
import doobie.hikari.HikariTransactor
import org.scalatest.Suite
import doobie.hikari.implicits._
import doobie.Transactor
import org.flywaydb.core.Flyway

trait PostgresBackedSpec extends ForAllTestContainer { this: Suite =>

  override val container = PostgreSQLContainer()

  implicit lazy val transactor: Transactor[IO] = hikariXA

  protected lazy val hikariXA: HikariTransactor[IO] =
    HikariTransactor
      .newHikariTransactor[IO](
        driverClassName = "org.postgresql.Driver",
        url             = container.jdbcUrl,
        user            = container.username,
        pass            = container.password
      )
      .unsafeRunSync()

  override def afterStart(): Unit = {
    Flyway
      .configure()
      .dataSource(container.jdbcUrl, container.username, container.password)
      .locations("filesystem:./sql/")
      .load()
      .migrate()
  }

  override def beforeStop(): Unit = {
    hikariXA.shutdown.unsafeRunSync()
  }

}

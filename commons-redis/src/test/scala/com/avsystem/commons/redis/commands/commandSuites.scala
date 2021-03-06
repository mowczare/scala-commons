package com.avsystem.commons
package redis.commands

import com.avsystem.commons.redis._

import scala.concurrent.duration._

trait KeyedFullApiSuite extends CommandsSuite
  with GeoApiSuite
  with KeyedScriptingApiSuite
  with KeyedKeysApiSuite
  with StringsApiSuite
  with HashesApiSuite
  with SortedSetsApiSuite
  with ListsApiSuite
  with SetsApiSuite
  with HyperLogLogApiSuite

trait NodeFullApiSuite extends KeyedFullApiSuite
  with NodeKeysApiSuite
  with ServerApiSuite
  with NodeScriptingApiSuite

trait ConnectionFullApiSuite extends NodeFullApiSuite
  with ConnectionScriptingApiSuite
  with BlockingListsApiSuite

class RedisClusterCommandsTest extends RedisClusterCommandsSuite with KeyedFullApiSuite
class RedisNodeCommandsTest extends RedisNodeCommandsSuite with NodeFullApiSuite
class RedisConnectionCommandsTest extends RedisConnectionCommandsSuite with ConnectionFullApiSuite

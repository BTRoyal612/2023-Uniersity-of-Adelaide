import unittest
from src.server import MultiThreadedServer

class TestServer(unittest.TestCase):

    def test_server_initialization(self):
        server = MultiThreadedServer("localhost", 12345)
        self.assertEqual(server.host, "localhost")
        self.assertEqual(server.port, 12345)

    # This is a very basic test. In real scenarios, you'd want to test the server's ability to accept connections, handle data, etc.
    # Those tests are a bit more complex and often require mocking or integration testing setups.

if __name__ == "__main__":
    unittest.main()

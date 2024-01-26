import unittest
from src.node import Node

class TestNode(unittest.TestCase):

    def test_node_initialization(self):
        node = Node("data")
        self.assertEqual(node.data, "data")
        self.assertIsNone(node.next)
        self.assertIsNone(node.book_next)
        self.assertIsNone(node.next_frequent_search)

    def test_node_data_assignment(self):
        node = Node("data")
        node.data = "new_data"
        self.assertEqual(node.data, "new_data")

    # Additional tests can be added here ...

if __name__ == "__main__":
    unittest.main()

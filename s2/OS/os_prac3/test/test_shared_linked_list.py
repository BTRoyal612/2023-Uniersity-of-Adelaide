import unittest
from src.shared_linked_list import SharedLinkedList
from src.node import Node

class TestSharedLinkedList(unittest.TestCase):

    def setUp(self):
        self.list = SharedLinkedList()

    def test_initialization(self):
        self.assertIsNone(self.list.head)

    def test_add_node(self):
        node = Node("data")
        self.list.add(node)
        self.assertEqual(self.list.head.data, "data")

    def test_get_last_node(self):
        node1 = Node("data1")
        node2 = Node("data2")
        self.list.add(node1)
        self.list.add(node2)
        last_node = self.list.get_last_node()
        self.assertEqual(last_node.data, "data2")

    # Additional tests can be added here ...

if __name__ == "__main__":
    unittest.main()

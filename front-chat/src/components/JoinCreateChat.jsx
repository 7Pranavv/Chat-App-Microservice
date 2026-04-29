import React, { useState } from "react";
import chatIcon from "../assets/chat.png";
import toast from "react-hot-toast";
import { createRoomApi, joinChatApi } from "../services/RoomService";
import useChatContext from "../context/ChatContext";
import { useNavigate } from "react-router";

const JoinCreateChat = () => {
  // ✅ FIX: Use separate state variables instead of a single object
  // The original single-object state with spread {...detail} can cause
  // stale closure issues where updates don't reflect before button click.
  const [roomId, setRoomId] = useState("");
  const [userName, setUserName] = useState("");

  const {
    setRoomId: setContextRoomId,
    setCurrentUser,
    setConnected,
  } = useChatContext();

  const navigate = useNavigate();

  function validateForm() {
    if (roomId.trim() === "" || userName.trim() === "") {
      toast.error("Invalid Input !! Please enter both name and room ID.");
      return false;
    }
    return true;
  }

  async function joinChat() {
    if (!validateForm()) return;
    try {
      const room = await joinChatApi(roomId);
      toast.success("Joined successfully!");
      setCurrentUser(userName);
      setContextRoomId(room.roomId);
      setConnected(true);
      navigate("/chat");
    } catch (error) {
      console.error("Join error:", error);
      if (error.response?.status === 400) {
        toast.error(error.response.data || "Room not found!");
      } else {
        toast.error("Error in joining room");
      }
    }
  }

  async function createRoom() {
    if (!validateForm()) return;
    try {
      const response = await createRoomApi(roomId);
      toast.success("Room Created Successfully!");
      setCurrentUser(userName);
      setContextRoomId(response.roomId);
      setConnected(true);
      navigate("/chat");
    } catch (error) {
      console.error("Create error:", error);
      if (error.response?.status === 400) {
        toast.error("Room already exists!");
      } else {
        toast.error("Error in creating room");
      }
    }
  }

  return (
    <div className="min-h-screen flex items-center justify-center">
      <div className="p-10 dark:border-gray-700 border w-full flex flex-col gap-5 max-w-md rounded dark:bg-gray-900 shadow">
        <div>
          <img src={chatIcon} className="w-24 mx-auto" />
        </div>

        <h1 className="text-2xl font-semibold text-center">
          Join Room / Create Room
        </h1>

        {/* Name input */}
        <div>
          <label htmlFor="userName" className="block font-medium mb-2">
            Your Name
          </label>
          <input
            type="text"
            id="userName"
            name="userName"
            placeholder="Enter your name"
            value={userName}
            onChange={(e) => setUserName(e.target.value)}
            className="w-full dark:bg-gray-600 px-4 py-2 border dark:border-gray-600 rounded-full focus:outline-none focus:ring-2 focus:ring-blue-500"
          />
        </div>

        {/* Room ID input */}
        <div>
          <label htmlFor="roomId" className="block font-medium mb-2">
            Room ID / New Room ID
          </label>
          <input
            type="text"
            id="roomId"
            name="roomId"
            placeholder="Enter the room ID"
            value={roomId}
            onChange={(e) => setRoomId(e.target.value)}
            className="w-full dark:bg-gray-600 px-4 py-2 border dark:border-gray-600 rounded-full focus:outline-none focus:ring-2 focus:ring-blue-500"
          />
        </div>

        {/* Buttons */}
        <div className="flex justify-center gap-2 mt-4">
          <button
            onClick={joinChat}
            className="px-3 py-2 bg-blue-500 hover:bg-blue-700 dark:bg-blue-500 hover:dark:bg-blue-800 text-white rounded-full"
          >
            Join Room
          </button>
          <button
            onClick={createRoom}
            className="px-3 py-2 bg-orange-500 hover:bg-orange-700 dark:bg-orange-500 hover:dark:bg-orange-800 text-white rounded-full"
          >
            Create Room
          </button>
        </div>
      </div>
    </div>
  );
};

export default JoinCreateChat;

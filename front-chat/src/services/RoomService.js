import { httpClient } from "../config/AxiosHelper";

// ── Room Service (via Gateway → port 8081) ────────────────────────────────
export const createRoomApi = async (roomDetail) => {
  const response = await httpClient.post(
    `/api/v1/rooms`,
    { roomId: roomDetail },
    { headers: { "Content-Type": "application/json" } }
  );
  return response.data;
};

export const joinChatApi = async (roomId) => {
  const response = await httpClient.get(`/api/v1/rooms/${roomId}`);
  return response.data;
};

// ── Message Service (via Gateway → port 8082) ─────────────────────────────
// Messages are now stored independently in Message Service, not inside Room.
export const getMessagess = async (roomId, size = 50, page = 0) => {
  const response = await httpClient.get(
    `/api/v1/messages/${roomId}?size=${size}&page=${page}`
  );
  return response.data;
};

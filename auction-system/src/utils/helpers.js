export const apiCall = async (endpoint, data) => {
  console.log(`API Call to ${endpoint} with data:`, data);
  return { success: true, message: "Operation successful" };
};

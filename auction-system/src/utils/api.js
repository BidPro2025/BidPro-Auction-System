import api from '../api/axios';

export const apiCall = async (endpoint, options = {}) => {
  try {
    const method = options.method || 'GET';
    const { data } = await api({
      url: endpoint,
      method,
      data: options.body,
      headers: options.headers,
    });
    return { success: true, data: data?.data ?? data, message: data?.message };
  } catch (err) {
    return {
      success: false,
      message: err.response?.data?.message || err.message,
    };
  }
};
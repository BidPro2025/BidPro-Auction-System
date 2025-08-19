import { useState } from "react";

const useNotification = () => {
  const [notification, setNotification] = useState({
    show: false,
    type: "",
    title: "",
    message: "",
  });

  const showNotification = (type, title, message) => {
    setNotification({ show: true, type, title, message });
    setTimeout(() => {
      setNotification((prev) => ({ ...prev, show: false }));
    }, 3000);
  };

  return { notification, showNotification, setNotification };
};

export default useNotification;

// ParentComponent.jsx
import ModalProfile from './ModalProfile';

function ParentComponent() {
  const apiCall = async (endpoint, options) => {
    // Your API call implementation here
    try {
      const response = await fetch(`/api${endpoint}`, options);
      return await response.json();
    } catch (error) {
      console.error('API call failed:', error);
      throw error;
    }
  };

  return (
    <ModalProfile
      show={showProfileModal}
      toggleModal={toggleModalFunction}
      apiCall={apiCall}  // Make sure this is passed
    />
  );
}
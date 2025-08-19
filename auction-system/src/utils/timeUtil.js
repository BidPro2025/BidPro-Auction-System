export const calculateTimeLeft = (endTime, now) => {
  const timeDiff = endTime - now;
  if (timeDiff <= 0) {
    return "Ended";
  }
  const days = Math.floor(timeDiff / (1000 * 60 * 60 * 24));
  const hours = Math.floor(
    (timeDiff % (1000 * 60 * 60 * 24)) / (1000 * 60 * 60)
  );
  const minutes = Math.floor((timeDiff % (1000 * 60 * 60)) / (1000 * 60));
  return days > 0 ? `${days}d ${hours}h` : `${hours}h ${minutes}m`;
};

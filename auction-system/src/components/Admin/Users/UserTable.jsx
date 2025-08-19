import React from 'react';

const UserTable = ({ users = [], handleAction, handleStatusUpdate }) => {
  if (!Array.isArray(users) || users.length === 0) {
    return (
      <div className="card-body p-0">
        <div className="table-responsive">
          <table className="table table-hover mb-0">
            <thead className="table-light">
              <tr>
                <th>User</th>
                <th>Type</th>
                <th>Status</th>
                <th>Joined</th>
                <th>Actions</th>
              </tr>
            </thead>
            <tbody>
              <tr>
                <td colSpan="5" className="text-center">No users found</td>
              </tr>
            </tbody>
          </table>
        </div>
      </div>
    );
  }

  return (
    <div className="card-body p-0">
      <div className="table-responsive">
        <table className="table table-hover mb-0">
          <thead className="table-light">
            <tr>
              <th>User</th>
              <th>Type</th>
              <th>Status</th>
              <th>Joined</th>
              <th>Actions</th>
            </tr>
          </thead>
          <tbody>
            {users.map(user => {
              const type = user.role === 'ADMIN' ? 'Admin'
                : user.role === 'SELLER' ? 'Seller'
                  : user.role === 'BOTH' ? 'Both'
                    : 'Bidder';

              return (
                <tr key={user.userID}>
                  <td>
                    <div className="d-flex align-items-center">
                      <div
                        className="rounded-circle d-flex align-items-center justify-content-center text-white fw-bold me-3 bg-secondary"
                        style={{ width: '40px', height: '40px' }}
                      >
                        {(user.firstName || '').charAt(0).toUpperCase()}
                      </div>
                      <div>
                        <h6 className="mb-0">{`${user.firstName || ''} ${user.lastName || ''} `}</h6>
                        <small className="text-muted">{user.email || '—'}</small>
                      </div>
                    </div>
                  </td>
                  <td>
                    <span
                      className={`badge ${type === 'Admin' ? 'bg-danger'
                          : type === 'Seller' ? 'bg-info'
                            : type === 'Both' ? 'bg-warning'
                              : 'bg-primary'
                        } `}
                    >
                      {type}
                    </span>
                  </td>
                  <td>
                    <select
                      className="form-select form-select-sm"
                      value={user.status || 'PENDING'}
                      onChange={(e) => handleStatusUpdate(user.userID, e.target.value)}
                    >
                      <option value="PENDING">Pending</option>
                      <option value="APPROVED">Approved</option>
                      <option value="SUSPENDED">Suspended</option>
                    </select>
                  </td>
                  <td>{user.createdDate ? new Date(user.createdDate).toLocaleDateString() : '—'}</td>
                  <td>
                    <button
                      className="btn btn-sm btn-outline-primary me-1"
                      onClick={() => handleAction('User View', `Loading user profile for ${user.firstName}`)}
                      title="View"
                    >
                      <i className="fas fa-eye"></i>
                    </button>
                    <button
                      className="btn btn-sm btn-outline-success me-1"
                      onClick={() => handleAction('User Edit', `Editing user ${user.firstName} `)}
                      title="Edit"
                    >
                      <i className="fas fa-edit"></i>
                    </button>
                    <button
                      className="btn btn-sm btn-outline-danger"
                      onClick={() => handleAction('User Delete', `Deleted user ${user.firstName} `)}
                      title="Delete"
                    >
                      <i className="fas fa-trash"></i>
                    </button>
                  </td>
                </tr>
              );
            })}
          </tbody>
        </table>
      </div>
    </div>
  );
};

export default UserTable;

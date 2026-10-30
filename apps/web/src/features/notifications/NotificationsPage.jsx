import { useEffect } from 'react';
import { Link } from 'react-router-dom';
import useResource from '../../shared/hooks/useResource';
import useMutation from '../../shared/hooks/useMutation';
import { request } from '../../shared/api/client';
import { dateTime, label } from '../../shared/lib/format';
import { Feedback, PageHeading, ResourceState } from '../../shared/components/UI';

export default function NotificationsPage() {
  const resource = useResource('/notifications');
  const mutation = useMutation();
  useEffect(() => {
    const timer = setInterval(resource.reload, 30000);
    return () => clearInterval(timer);
  }, [resource.reload]);
  const mark = async (item) => {
    const result = await mutation.run(
      () => request(`/notifications/${item.notificationID}/read`, { method: 'PUT' }),
      'Notification marked as read.',
    );
    if (result.ok) {
      resource.reload();
      window.dispatchEvent(new Event('bidpro:notifications-updated'));
    }
  };
  const markAll = async () => {
    const result = await mutation.run(
      () => request('/notifications/read-all', { method: 'PUT' }),
      'All notifications marked as read.',
    );
    if (result.ok) {
      resource.reload();
      window.dispatchEvent(new Event('bidpro:notifications-updated'));
    }
  };
  return (
    <>
      <PageHeading
        eyebrow="YOUR ACCOUNT"
        title="Notifications"
        action={
          <div className="d-flex gap-2">
            <button className="btn btn-outline-secondary" onClick={resource.reload}>
              Refresh
            </button>
            <button
              className="btn btn-primary"
              disabled={mutation.pending || !resource.data?.some((item) => !item.isRead)}
              onClick={markAll}
            >
              Mark all as read
            </button>
          </div>
        }
      >
        Account updates, auction activity and messages in one place.
      </PageHeading>
      <Feedback mutation={mutation} />
      <ResourceState
        resource={resource}
        isEmpty={!resource.data?.length}
        empty="You are all caught up"
      >
        <ul className="notification-list">
          {(resource.data || []).map((item) => (
            <li
              key={item.notificationID}
              className={`surface ${item.isRead ? '' : 'notification-unread'}`}
            >
              <div className="d-flex justify-content-between flex-wrap gap-2">
                <span className="eyebrow mb-0">{label(item.type)}</span>
                <time className="small text-secondary">{dateTime(item.timestamp)}</time>
              </div>
              <p className="preserve-lines mt-2 mb-3">{item.content}</p>
              <div className="d-flex gap-3 align-items-center">
                {item.link?.startsWith('/') && !item.link.startsWith('//') && (
                  <Link
                    to={item.link}
                    onClick={() => {
                      if (!item.isRead) mark(item);
                    }}
                  >
                    View details <i className="bi bi-arrow-right" />
                  </Link>
                )}
                {!item.isRead && (
                  <button
                    className="btn btn-outline-secondary btn-sm"
                    disabled={mutation.pending}
                    onClick={() => mark(item)}
                  >
                    Mark as read
                  </button>
                )}
              </div>
            </li>
          ))}
        </ul>
      </ResourceState>
    </>
  );
}

import { useState } from 'react';
import useResource from '../../shared/hooks/useResource';
import useMutation from '../../shared/hooks/useMutation';
import { request } from '../../shared/api/client';
import { dateTime } from '../../shared/lib/format';
import { Badge, Feedback, Notice, PageHeading, ResourceState } from '../../shared/components/UI';

export default function MailOutboxPage() {
  const resource = useResource('/admins/email-outbox');
  const mutation = useMutation();
  const [selected, setSelected] = useState(null);
  const email = (resource.data || []).find((item) => item.id === selected);
  const retry = async (item) => {
    const result = await mutation.run(
      () => request(`/admins/email-outbox/${item.id}/retry`, { method: 'POST' }),
      'Email queued for delivery.',
    );
    if (result.ok) resource.reload();
  };
  const resetLink = email?.body?.match(
    /https?:\/\/[^\s<>"']+\/reset-password\?token=[A-Za-z0-9_-]+/,
  )?.[0];
  return (
    <>
      <PageHeading
        eyebrow="ADMINISTRATOR WORKSPACE"
        title="Email outbox"
        action={
          <button className="btn btn-outline-secondary" onClick={resource.reload}>
            Refresh
          </button>
        }
      >
        Review delivery status and retry failed messages.
      </PageHeading>
      <Feedback mutation={mutation} />
      {resource.data?.some((item) => item.status === 'LOCAL') && (
        <Notice type="info">
          Local email preview is enabled. Messages marked Local are stored here instead of being
          sent to an external mailbox.
        </Notice>
      )}
      <ResourceState resource={resource} isEmpty={!resource.data?.length} empty="No emails yet">
        <div className="surface table-responsive">
          <table className="table align-middle">
            <thead>
              <tr>
                <th scope="col">Recipient</th>
                <th scope="col">Subject</th>
                <th scope="col">Created</th>
                <th scope="col">Status</th>
                <th scope="col">Actions</th>
              </tr>
            </thead>
            <tbody>
              {(resource.data || []).map((item) => (
                <tr key={item.id}>
                  <td className="break-word">{item.recipient}</td>
                  <td>{item.subject}</td>
                  <td>{dateTime(item.createdAt)}</td>
                  <td>
                    <Badge value={item.status} />
                  </td>
                  <td>
                    <div className="d-flex gap-2">
                      <button
                        className="btn btn-outline-primary btn-sm"
                        onClick={() => setSelected(item.id)}
                      >
                        View email
                      </button>
                      {['FAILED', 'QUEUED'].includes(item.status) && (
                        <button
                          className="btn btn-outline-secondary btn-sm"
                          disabled={mutation.pending}
                          onClick={() => retry(item)}
                        >
                          Retry delivery
                        </button>
                      )}
                    </div>
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      </ResourceState>
      {email && (
        <section className="surface mt-4" aria-label="Email preview">
          <div className="d-flex justify-content-between align-items-start gap-2">
            <div>
              <h2>{email.subject}</h2>
              <p className="small text-secondary">To: {email.recipient}</p>
            </div>
            <button className="btn btn-outline-secondary btn-sm" onClick={() => setSelected(null)}>
              Close preview
            </button>
          </div>
          <p className="preserve-lines break-word">{email.body}</p>
          {email.status === 'LOCAL' && resetLink && (
            <a className="btn btn-outline-primary btn-sm" href={resetLink}>
              Open local password reset link
            </a>
          )}
          {email.lastError && <Notice>{email.lastError}</Notice>}
          <p className="small text-secondary mb-0">
            Delivery attempts: {email.attempts || 0}
            {email.sentAt && ` - Sent ${dateTime(email.sentAt)}`}
          </p>
        </section>
      )}
    </>
  );
}

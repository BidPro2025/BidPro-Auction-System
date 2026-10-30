import { useEffect, useState } from 'react';
import { Link, useNavigate, useParams } from 'react-router-dom';
import { useAuth } from '../auth/AuthContext';
import { request } from '../../shared/api/client';
import useResource from '../../shared/hooks/useResource';
import useMutation from '../../shared/hooks/useMutation';
import { dateTime } from '../../shared/lib/format';
import {
  Badge,
  Empty,
  Feedback,
  Field,
  Notice,
  PageHeading,
  ResourceState,
} from '../../shared/components/UI';

export default function InboxPage() {
  const { user } = useAuth();
  const { threadID } = useParams();
  const navigate = useNavigate();
  const threads = useResource(user.status === 'APPROVED' ? '/conversations' : null);
  const conversation = useResource(
    threadID && user.status === 'APPROVED'
      ? `/conversations/${encodeURIComponent(threadID)}`
      : null,
  );
  const recipients = useResource(user.role === 'ADMIN' ? '/conversations/recipients' : null);
  const mutation = useMutation();
  const [creating, setCreating] = useState(false);
  const [form, setForm] = useState({ subject: '', message: '', recipientUserID: '' });
  const [reply, setReply] = useState('');
  useEffect(() => {
    setReply('');
    setCreating(false);
  }, [threadID]);
  const reloadThreads = threads.reload;
  const reloadConversation = conversation.reload;
  useEffect(() => {
    const timer = setInterval(() => {
      if (document.visibilityState !== 'visible') return;
      reloadThreads();
      if (threadID) reloadConversation();
    }, 15000);
    return () => clearInterval(timer);
  }, [reloadThreads, reloadConversation, threadID]);
  const start = async (event) => {
    event.preventDefault();
    const result = await mutation.run(
      () =>
        request('/conversations', {
          method: 'POST',
          body: {
            subject: form.subject.trim(),
            message: form.message.trim(),
            ...(user.role === 'ADMIN' ? { recipientUserID: form.recipientUserID } : {}),
          },
        }),
      'Conversation started.',
    );
    if (result.ok) {
      setCreating(false);
      setForm({ subject: '', message: '', recipientUserID: '' });
      threads.reload();
      navigate(`/messages/${result.data.threadID}`);
    }
  };
  const send = async (event) => {
    event.preventDefault();
    const result = await mutation.run(
      () =>
        request(`/conversations/${encodeURIComponent(threadID)}/messages`, {
          method: 'POST',
          body: { content: reply.trim() },
        }),
      'Message sent.',
    );
    if (result.ok) {
      setReply('');
      conversation.reload();
      threads.reload();
    }
  };
  const changeStatus = async () => {
    const status = conversation.data.status === 'OPEN' ? 'CLOSED' : 'OPEN';
    const result = await mutation.run(
      () =>
        request(`/conversations/${encodeURIComponent(threadID)}/status`, {
          method: 'PUT',
          body: { status },
        }),
      status === 'OPEN' ? 'Conversation reopened.' : 'Conversation closed.',
    );
    if (result.ok) {
      conversation.reload();
      threads.reload();
    }
  };
  const choose = (id) => {
    setCreating(false);
    setReply('');
    navigate(`/messages/${id}`);
  };
  return (
    <>
      <PageHeading
        eyebrow="MARKETPLACE SUPPORT"
        title="Messages"
        action={
          <button
            className="btn btn-primary"
            disabled={user.status !== 'APPROVED'}
            onClick={() => {
              setCreating(true);
              setReply('');
            }}
          >
            New conversation
          </button>
        }
      >
        Private conversations between your account and the marketplace support team.
      </PageHeading>
      <Feedback mutation={mutation} />
      {user.status !== 'APPROVED' ? (
        <Notice type="warning">Messaging becomes available after your account is approved.</Notice>
      ) : (
        <div className="messages-grid">
          <aside className="surface conversation-list">
            <div className="d-flex justify-content-between align-items-center mb-3">
              <h2 className="mb-0">Conversations</h2>
              <button className="btn btn-outline-secondary btn-sm" onClick={threads.reload}>
                Refresh
              </button>
            </div>
            <ResourceState
              resource={threads}
              isEmpty={!threads.data?.length}
              empty="No conversations yet"
            >
              <div className="d-flex flex-column gap-2">
                {(threads.data || []).map((thread) => (
                  <button
                    key={thread.threadID}
                    className={`conversation-item ${threadID === thread.threadID && !creating ? 'selected' : ''}`}
                    onClick={() => choose(thread.threadID)}
                  >
                    <span className="d-flex justify-content-between gap-2">
                      <strong>{thread.subject}</strong>
                      {thread.unreadCount > 0 && (
                        <span
                          className="badge text-bg-success"
                          aria-label={`${thread.unreadCount} unread messages`}
                        >
                          {thread.unreadCount}
                        </span>
                      )}
                    </span>
                    {user.role === 'ADMIN' && (
                      <span className="small text-secondary">{thread.ownerName}</span>
                    )}
                    <span className="small text-secondary">
                      {dateTime(thread.updatedAt)} - {thread.status === 'OPEN' ? 'Open' : 'Closed'}
                    </span>
                  </button>
                ))}
              </div>
            </ResourceState>
          </aside>
          <section className="surface">
            {creating ? (
              <>
                <h2>Start a conversation</h2>
                <form onSubmit={start}>
                  <fieldset disabled={mutation.pending}>
                    {user.role === 'ADMIN' && (
                      <>
                        <Field label="Recipient" name="recipientUserID">
                          <select
                            className="form-select"
                            id="recipientUserID"
                            required
                            value={form.recipientUserID}
                            onChange={(event) =>
                              setForm({ ...form, recipientUserID: event.target.value })
                            }
                          >
                            <option value="">Choose an account</option>
                            {(recipients.data || []).map((recipient) => (
                              <option key={recipient.userID} value={recipient.userID}>
                                {recipient.name} - {recipient.email}
                              </option>
                            ))}
                          </select>
                        </Field>
                        <Notice>{recipients.error}</Notice>
                      </>
                    )}
                    <Field
                      label="Subject"
                      name="conversationSubject"
                      value={form.subject}
                      onChange={(event) => setForm({ ...form, subject: event.target.value })}
                      maxLength={120}
                      required
                    />
                    <Field label="Your message" name="conversationMessage">
                      <textarea
                        className="form-control"
                        id="conversationMessage"
                        rows={5}
                        value={form.message}
                        onChange={(event) => setForm({ ...form, message: event.target.value })}
                        maxLength={4000}
                        required
                      />
                    </Field>
                    <div className="d-flex gap-2">
                      <button className="btn btn-primary">
                        {mutation.pending ? 'Sending...' : 'Start conversation'}
                      </button>
                      <button
                        type="button"
                        className="btn btn-outline-secondary"
                        onClick={() => setCreating(false)}
                      >
                        Cancel
                      </button>
                    </div>
                  </fieldset>
                </form>
              </>
            ) : threadID ? (
              <ResourceState resource={conversation}>
                {conversation.data && (
                  <>
                    <div className="d-flex flex-wrap justify-content-between align-items-start gap-3 mb-3">
                      <div>
                        <h2>{conversation.data.subject}</h2>
                        <Badge value={conversation.data.status} />
                        {user.role === 'ADMIN' && (
                          <p className="small text-secondary mb-0 mt-2">
                            {conversation.data.ownerName} - {conversation.data.ownerEmail}
                          </p>
                        )}
                      </div>
                      <button
                        className="btn btn-outline-secondary btn-sm"
                        disabled={mutation.pending}
                        onClick={changeStatus}
                      >
                        {conversation.data.status === 'OPEN'
                          ? 'Close conversation'
                          : 'Reopen conversation'}
                      </button>
                    </div>
                    <ol className="message-history" aria-label="Conversation messages">
                      {conversation.data.messages.map((message) => (
                        <li
                          key={message.messageID}
                          className={message.senderID === user.userID ? 'own-message' : ''}
                        >
                          <div className="d-flex justify-content-between gap-3 mb-1">
                            <strong>
                              {message.senderID === user.userID
                                ? 'You'
                                : message.fromAdmin
                                  ? `${message.senderName} - Support`
                                  : message.senderName}
                            </strong>
                            <time className="small text-secondary">
                              {dateTime(message.createdAt)}
                            </time>
                          </div>
                          <p className="preserve-lines mb-0">{message.content}</p>
                        </li>
                      ))}
                    </ol>
                    {conversation.data.status === 'OPEN' ? (
                      <form onSubmit={send}>
                        <Field label="Reply" name="replyMessage">
                          <textarea
                            className="form-control"
                            id="replyMessage"
                            rows={4}
                            value={reply}
                            onChange={(event) => setReply(event.target.value)}
                            maxLength={4000}
                            required
                          />
                        </Field>
                        <button
                          className="btn btn-primary"
                          disabled={mutation.pending || !reply.trim()}
                        >
                          {mutation.pending ? 'Sending...' : 'Send reply'}
                        </button>
                      </form>
                    ) : (
                      <Notice type="light">
                        This conversation is closed. Reopen it to send another message.
                      </Notice>
                    )}
                  </>
                )}
              </ResourceState>
            ) : (
              <Empty title="Choose a conversation">
                <Link to="/messages" onClick={() => setCreating(true)}>
                  Start a new conversation with support
                </Link>
              </Empty>
            )}
          </section>
        </div>
      )}
    </>
  );
}

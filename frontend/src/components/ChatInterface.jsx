import { useState, useRef, useEffect } from 'react';

const EXAMPLE_PROMPTS = [
    "Show me errors in transaction logs",
    "What errors are in the zt1030 files?",
    "Why did the payment batch fail?",
    "Show me all errors from the last hour"
];

const API_BASE_URL = 'http://localhost:8082';

/**
 * ChatInterface Component
 * Renders the main chat UI for interacting with the Splunk Intelligent Layer.
 * Manages message state, input handling, and backend API communication.
 */
export default function ChatInterface() {
    const [messages, setMessages] = useState([
        { id: 1, type: 'bot', content: 'Hello! I am your Splunk AI Assistant. Ask me anything about your logs.' }
    ]);
    const [input, setInput] = useState('');
    const [isLoading, setIsLoading] = useState(false);
    const [queryHistory, setQueryHistory] = useState([]);
    const [reports, setReports] = useState([]);
    const messagesEndRef = useRef(null);

    // Fetch query history and reports on component mount
    useEffect(() => {
        fetchQueryHistory();
        fetchReports();
    }, []);

    const fetchQueryHistory = async () => {
        try {
            const response = await fetch(`${API_BASE_URL}/api/logs/history`);
            if (response.ok) {
                const data = await response.json();
                setQueryHistory(data);
            }
        } catch (error) {
            console.error('Error fetching query history:', error);
        }
    };

    const fetchReports = async () => {
        try {
            const response = await fetch(`${API_BASE_URL}/api/logs/reports`);
            if (response.ok) {
                const data = await response.json();
                setReports(data);
            }
        } catch (error) {
            console.error('Error fetching reports:', error);
        }
    };

    const handleReportClick = (reportName) => {
        setInput(`Run the report called "${reportName}"`);
    };

    /**
     * Scrolls the chat window to the bottom.
     * Triggered automatically when new messages are added.
     */
    const scrollToBottom = () => {
        messagesEndRef.current?.scrollIntoView({ behavior: "smooth" });
    };

    useEffect(scrollToBottom, [messages]);

    /**
     * Handles the submission of a user message.
     * Sends the question to the backend and updates the UI with the response.
     * 
     * @param {Event} e - The form submission event.
     */
    const handleSubmit = async (e) => {
        e?.preventDefault();
        if (!input.trim() || isLoading) return;

        const userMessage = { id: Date.now(), type: 'user', content: input };
        setMessages(prev => [...prev, userMessage]);
        setInput('');
        setIsLoading(true);

        try {
            const response = await fetch(`${API_BASE_URL}/api/logs/analyze`, {
                method: 'POST',
                headers: { 'Content-Type': 'application/json' },
                body: JSON.stringify({ question: userMessage.content }),
            });

            if (!response.ok) throw new Error('Network response was not ok');

            const data = await response.json();

            const botMessage = {
                id: Date.now() + 1,
                type: 'bot',
                content: data.aiSummary,
                spl: data.generatedSpl
            };

            setMessages(prev => [...prev, botMessage]);
            // Refresh query history after successful query
            fetchQueryHistory();
        } catch (error) {
            console.error('Error:', error);
            setMessages(prev => [...prev, {
                id: Date.now() + 1,
                type: 'bot',
                content: 'Sorry, I encountered an error processing your request. Please ensure the backend is running.'
            }]);
        } finally {
            setIsLoading(false);
        }
    };

    const handleHistoryClick = (question) => {
        setInput(question);
    };

    return (
        <div className="app-layout">
            <div className="chat-container">
                <div className="chat-header">
                    <h1>Splunk Intelligent Layer</h1>
                </div>

                <div className="chat-messages">
                {messages.map((msg) => (
                    <div key={msg.id} className={`message ${msg.type}`}>
                        <div>{msg.content}</div>
                        {msg.spl && (
                            <div className="spl-code">
                                SPL: {msg.spl}
                            </div>
                        )}
                    </div>
                ))}
                {isLoading && (
                    <div className="message bot">
                        <span className="loading-dots">Thinking...</span>
                    </div>
                )}
                <div ref={messagesEndRef} />
            </div>

            <div className="example-prompts">
                {EXAMPLE_PROMPTS.map((prompt, i) => (
                    <div key={i} className="example-chip" onClick={() => setInput(prompt)}>
                        {prompt}
                    </div>
                ))}
            </div>

            <div className="input-area">
                <form className="input-wrapper" onSubmit={handleSubmit}>
                    <textarea
                        value={input}
                        onChange={(e) => setInput(e.target.value)}
                        onKeyDown={(e) => {
                            if (e.key === 'Enter' && !e.shiftKey) {
                                e.preventDefault();
                                handleSubmit();
                            }
                        }}
                        placeholder="Ask a question about your logs..."
                        rows={1}
                    />
                    <button type="button" className="send-btn" onClick={handleSubmit} disabled={isLoading}>
                        <svg width="24" height="24" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round">
                            <line x1="22" y1="2" x2="11" y2="13"></line>
                            <polygon points="22 2 15 22 11 13 2 9 22 2"></polygon>
                        </svg>
                    </button>
                </form>
            </div>
        </div>

            {/* Right Sidebar */}
            <div className="right-sidebar">
                {/* Available Reports */}
                <div className="reports-panel">
                    <h3>Available Reports</h3>
                    {reports.length > 0 ? (
                        <ul>
                            {reports.map((report, index) => (
                                <li key={index} onClick={() => handleReportClick(report)}>
                                    <span className="report-name">{report}</span>
                                </li>
                            ))}
                        </ul>
                    ) : (
                        <p className="no-reports">No reports available</p>
                    )}
                </div>

                {/* Query History */}
                <div className="query-history-sidebar">
                    <h3>Recent Queries</h3>
                    {queryHistory.length > 0 ? (
                        <ul>
                            {queryHistory.slice(0, 10).map((item) => (
                                <li key={item.id} onClick={() => handleHistoryClick(item.question)}>
                                    <span className="history-question">{item.question}</span>
                                    <span className="history-time">
                                        {new Date(item.timestamp).toLocaleTimeString()}
                                    </span>
                                </li>
                            ))}
                        </ul>
                    ) : (
                        <p className="no-history">No queries yet</p>
                    )}
                </div>
            </div>
        </div>
    );
}

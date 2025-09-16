import { bind } from 'decko';
import { Component, h } from 'preact';
import { Xterm, XtermOptions } from './xterm';

import '@xterm/xterm/css/xterm.css';
import { Modal } from '../modal';

interface Props extends XtermOptions {
    id: string;
}

interface State {
    modal: boolean;
}

export class Terminal extends Component<Props, State> {
    private container: HTMLElement;
    private xterm: Xterm;

    constructor(props: Props) {
        super();
        this.xterm = new Xterm(props, this.showModal);
    }

    async componentDidMount() {
        await this.xterm.refreshToken();
        this.xterm.open(this.container);
        this.xterm.connect();
    }

    componentWillUnmount() {
        this.xterm.dispose();
    }

    render({ id }: Props, { modal }: State) {
        return (
            <div
                id={id}
                ref={c => {
                    this.container = c as HTMLElement;
                }}
                style={{
                    display: 'flex',
                }}
            >
                <div class="header">
                    <div class="title">🐬OurSQL Web Terminal</div>
                    <div>
                        <a href="https://github.com/lauvAri/our-sql">our repo</a>
                        <br />
                        <hr />
                        <p>Compiler</p>
                        <p>↓</p>
                        <p>Data Base</p>
                        <p>↓</p>
                        <p>Operating System</p>
                        <hr />
                        <pre>
                            <code>create table</code>
                            <br />
                            <code>insert into</code>
                            <br />
                            <code>delete from</code>
                            <br />
                            <code>order by</code>
                            <br />
                            <code>limit</code>
                            <br />
                            <code>...</code>
                        </pre>
                    </div>
                </div>
                <Modal show={modal}>
                    <label class="file-label">
                        <input onChange={this.sendFile} class="file-input" type="file" multiple />
                        <span class="file-cta">Choose files…</span>
                    </label>
                </Modal>
            </div>
        );
    }

    @bind
    showModal() {
        this.setState({ modal: true });
    }

    @bind
    sendFile(event: Event) {
        this.setState({ modal: false });
        const files = (event.target as HTMLInputElement).files;
        if (files) this.xterm.sendFile(files);
    }
}

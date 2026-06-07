# Repo instructions for Claude

## Workflow

- **Always raise a PR for changes.** Never commit code changes directly to `develop` (or `main`). Create a fresh branch off the latest `origin/develop`, commit there, push, and open a PR against `develop` with `gh pr create`. This applies to every change, no matter how small.

## Shell command style

Keep commands flat so the permission checker can match them cleanly.

- **No heredocs** (`$(cat <<EOF ...)` or `--body-file - <<'EOF' ...`). Write content to a file first, then pass the file path.
- **No `cd` prefixes** — run from the current working directory.
- **No `git -C "<path>"` for the current worktree** — run git directly from the working directory. The `-C` prefix defeats Claude Code's built-in read-only detection, so plain read-only commands (`git status`/`log`/`diff`/`branch`) start prompting every time. Only use `-C` to reach a *different* worktree.
- **No chaining commands with `&&`** — issue each as a separate command.
- **No command substitution** (`$(...)`, backticks) inside otherwise-allowed commands.
- **Short commit messages** (single line) can use `git commit -m 'short summary'` directly. For multi-line commit bodies, write to `.claude-tmp/COMMIT_MSG.md` first, then `git commit -F .claude-tmp/COMMIT_MSG.md`.
- **PR bodies:** write to `.claude-tmp/PR_BODY.md`, then `gh pr create … --body-file .claude-tmp/PR_BODY.md`.
- **PR comments:** write to `.claude-tmp/PR_COMMENT.md`, then `gh pr comment <n> --body-file .claude-tmp/PR_COMMENT.md`.
- **`gh` doesn't accept `-C`** — when the current working directory isn't inside the target repo, pass `--repo OWNER/NAME` and use an absolute path for `--body-file`.
- **Never write scratch files inside `.git/`** — it's treated as a sensitive path and triggers permission prompts. Use `.claude-tmp/` (gitignored at the repo root).

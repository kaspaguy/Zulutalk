# ZuluTalk for Android

ZuluTalk is a voice and video calling app for a private, self-hosted SIP server.
It is a fork of [Linphone Android](https://gitlab.linphone.org/BC/public/linphone-android)
6.2.8, by Belledonne Communications, and is released under the same GNU GPLv3 license.

Current version: **1.2** (versionCode 3), built on Linphone 6.2.8 and Linphone SDK 5.5.35.

## What it solves

Stock Linphone is a general-purpose SIP client. It is built for people who understand SIP, and
it ships with linphone.org services built in. ZuluTalk is for people who just want to make calls:

- **One-tap setup.** A person receives a link or a QR code. Opening it installs the app and
  configures their account through Linphone's remote provisioning (`linphone-config:` links).
  They never see SIP settings.
- **Calls only.** Features a calling-only service does not use (chat, meetings, call recording,
  linphone.org account creation) are hidden, so the app opens straight to the dial pad.
- **No third-party services by default.** The app does not contact linphone.org for update
  checks, log upload or file sharing.
- **Video that works on mobile data and foldable phones.** See versions 1.1 and 1.2 below.

## Changes from Linphone 6.2.8

### 1.0: rebrand and defaults
- Package `com.zulutalk.app`, so ZuluTalk installs alongside Linphone instead of replacing it.
- The app is named ZuluTalk in every language, with ZuluTalk icons on the launcher,
  notifications, splash screen, welcome screen and side menu.
- Default settings (`app/src/main/assets/linphonerc_default`):
  - linphone.org update check, log upload and file transfer server removed
  - linphone.org account creation removed, and the assistant defaults to a
    third-party account over TLS
  - chat, meetings and call recordings hidden; the dial pad opens first
  - `keep_service_alive=1`, so incoming calls ring while the app is in the background
    without push notifications
  - incoming video is accepted both ways (Linphone's default is receive-only)
- On an outgoing video call, the caller switches to the speaker when the call connects.
  Stock Linphone does this only for the person answering, so the caller stayed on the earpiece
  (`CurrentCallViewModel`).
- Linphone SDK pinned to `5.5.35` (stock `5.5.+` can resolve to a pre-release).
- arm64-v8a only.
- Linphone's `google-services.json` removed. ZuluTalk has no Firebase push yet.

### 1.1: smaller video on mobile data and slow links
- New `ZuluTalkBandwidthManager` watches the active network.
- On cellular, metered or slow links (upload under 1000 kbit/s), video is capped at
  384 kbit/s and QVGA. On fast unmetered networks it returns to VGA with no cap.
- If the network type changes during a video call (Wi-Fi ↔ mobile data), the call is
  renegotiated with the new limits.
- Why: with stock settings, video sent from a phone on mobile data froze. Round-trip times
  reached 2 s, and the other side kept asking for a fresh picture.

### 1.2: video on foldable phones
- `RoundCornersTextureView` recomputes its video transform when the view changes size.
- Why: folding a phone such as a Galaxy Fold during a call moved the video from the inner
  screen to the cover screen. The transform for the old size was kept, so only the middle of
  the picture was visible.

## Status

- 1.0 has been used for calls between Android phones over SIP-TLS with SRTP.
- 1.1 and 1.2 are being tested on real devices. The improvements above describe what each
  change is meant to fix. They have not been measured in the field yet.
- Known issue: a call hang has been reported on a foldable phone. The server showed media
  flowing both ways at the time. A fix is planned for 1.3, once there is an app log.

## Building

The build works the same way as upstream Linphone (see [README.md](README.md)).

```
ANDROID_HOME=/path/to/android-sdk ./gradlew ktlintCheck assembleRelease
```

Release builds are signed with a key from `keystore.properties`, which is not part of this
repository. Upstream changes are merged in from Linphone's `release/6.2` branch.

## Credits

ZuluTalk exists because of the work of [Belledonne Communications](https://www.linphone.org)
and the Linphone contributors. Almost all of the code in this repository is theirs.

- **Linphone Android** app: Copyright © 2010-2025 Belledonne Communications SARL.
  The full upstream commit history is kept in this repository, so every original author is
  credited for their work. ZuluTalk's own changes start after upstream commit `dcaa165b7`
  (tag 6.2.8).
- **Linphone SDK** 5.5.35 (liblinphone, mediastreamer2, belle-sip, oRTP, bctoolbox, bzrtp and
  more), by Belledonne Communications under GPLv3. It is used unmodified from Belledonne's
  Maven repository.
- **Third-party components** used by Linphone are listed by Belledonne at
  [Third party components](https://wiki.linphone.org/xwiki/wiki/public/view/Linphone/Third%20party%20components%20/#Hlinphone-android).
  This list is also linked from the app (Help → About).
- **Translations** come from the Linphone community on [Weblate](https://weblate.linphone.org/).

Every file changed for ZuluTalk keeps its original Belledonne copyright header and carries a
"Modified by Thanaaya Technologies for ZuluTalk" notice. New files are marked as ZuluTalk files.
The app's Help → About screen keeps the GPLv3 notice and the Belledonne copyright.

## Reporting problems and sending logs

**All ZuluTalk problem reports and error logs go to ZuluTalk, never to Linphone.**
Belledonne Communications does not support ZuluTalk, and ZuluTalk users' logs must not be sent
to them.

- Email: **reports@zulutalk.com**
- In the app (from 1.3): Help → Troubleshooting → Send debug logs opens an email to
  reports@zulutalk.com with the logs attached.
- On GitHub: open an issue in this repository. Logs can contain phone numbers and SIP
  addresses, so email them instead of attaching them to a public issue.

Only report a problem to Linphone if you can reproduce it in the unmodified Linphone app
from Belledonne.

## Links inside the app (changed for 1.3, not yet released)

In 1.2, some Help links still pointed to Linphone. In the source they now point to ZuluTalk:

| Link | 1.2 (released) | 1.3 (source) |
|---|---|---|
| Send debug logs (email) | Belledonne's Linphone support address | reports@zulutalk.com |
| User guide, contact | linphone.org | https://zulutalk.com/support/ |
| Privacy policy | linphone.org | https://zulutalk.com/privacy/ |
| Terms of use | linphone.org | https://zulutalk.com/terms/ |
| Download / invite | linphone.org | https://zulutalk.com/download/ |
| Contribute on translation | Linphone Weblate | https://zulutalk.com/credits/#translations (explains that translations come from Linphone's Weblate, and links there) |

Two more fixes so ZuluTalk does not use Belledonne's servers (also in 1.3, not yet released):

- **Log upload:** 1.2 sets Linphone's log server (`files.linphone.org`) when none is configured,
  so Help → Troubleshooting → Upload logs would send ZuluTalk logs to Belledonne. 1.3 never sets
  it and clears it on phones where 1.2 did. With no upload server, the Upload button is hidden,
  and logs are sent by email to reports@zulutalk.com instead.
- **STUN:** accounts added by hand in 1.2 use Belledonne's public STUN server
  (`stun.linphone.org`) by default. In 1.3 the default for third-party accounts is empty, so
  ZuluTalk does not use a free Linphone service on our behalf. Linphone.org accounts keep
  Linphone's own settings.

These stay unchanged, as credit to Belledonne: the GPLv3 notice, the
"© Belledonne Communications 2010-2025" line and the Linphone third-party components link
on the About screen.

## License

ZuluTalk is distributed under the GNU General Public License v3 (see [LICENSE.txt](LICENSE.txt)),
the same license Belledonne Communications uses for the open source release of Linphone.
Source code for every released ZuluTalk APK is tagged in this repository (`v1.0`, `v1.1`, `v1.2`).

"Linphone" is Belledonne Communications' product name. ZuluTalk is an independent fork. It is
not affiliated with, endorsed by or supported by Belledonne Communications, so please report
ZuluTalk problems here, not to Linphone.

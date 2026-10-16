# Helper cards

The cards the campaign has found for a need: a plain minion, a character of the wrong culture for his race, a printed card that does the thing a test has to see done. Each entry begins with the need in bold, then the card or cards with their ids, then what each is and what of its own text gets in a board's way. The gemp-testing skill lists every entry by its need, group by group; `python3 .claude/skills/card-test-campaign/scripts/lookup.py "<words of the need, a card's name or an id>" --in helpers` prints the entries that contain the words.

An entry is where to start, not a licence: the definition of every card that goes into a scenario is still read (`packet.py --defs <ids>`), since a helper's own text can change the game under a test. A new helper is one more paragraph under the group it belongs to, the need in bold at its head; an entry that is already there is sharpened and no second one added; `lookup.py --index` then lists it in the skill.

## Minions: plain ones, and ones with one keyword

**A minion that does nothing.** Goblin Patrol Troop 1_177. Strength 13, vitality 3, cost 6, site 4, no text at all.
It overwhelms (kills) any companion of strength 6 or less: see "A companion beaten by double his strength" in the
pitfalls. The minion to lay as the invalid candidate of an exert choice: one of vitality 1 (the Runner, the
Brigand) is never offered whatever the filter.

**A cheap minion.** Goblin Runner 1_178. Strength 5, vitality 1; when played, may add (2): harmless if cheated onto
the table, and added without asking when it is played for real (F-0009). Played beside a card with its own required
trigger on a minion being played (The Faithful Stone 68_50), the two come at once and the Free Peoples player
orders them in a "Required responses" decision: play Goblin Patrol Troop there. Its site number is 4: played at
sites 2 or 3 it is roaming and costs 1 + 2, before its own (2) is added (Card_12_111_Tests reads the pool so).

**A weak minion that survives a hit.** Uruk Savage 1_151. Strength 5, vitality 3; Damage +1 is its whole text. An
unboosted strength-6 companion beats it without killing it.

**A minion with damage +1 and nothing else.** Uruk-hai Raiding Party 1_158. Strength 9, vitality 3, cost 4, site 5;
Damage +1 is its whole text. With vitality 2: Orthanc Warrior 4_165 (strength 7, cost 2, site 5), killed by exactly
two wounds, which tells "wound twice" from one wound and from two exertions.

**A minion with damage +1 who is no Uruk-hai (beside "each Uruk-hai ... loses all damage bonuses").** Cave Troll of
Moria, Scourge of the Black Pit 1_165. Unique [moria] Troll, strength 15, vitality 4, cost 10, site 4; Damage +1
and fierce; "To play, spot a [moria] Orc" and a cost of -3 underground are about playing it: cheat it in
(Card_02_016_Tests). The race without the culture: Saruman, Of Many Colours 12_54, an [isengard] Wizard with damage
+1, fierce and lurker, whose named-culture strength -1 comes only when he is played.

**Two Wizard minions in one skirmish, with one Saruman (Q-0239).** Saruman, Coldly Still 17_48 beside Pallando
Deceived 13_79. Saruman: unique [men] Wizard, strength 8, vitality 4, site 4; he may be assigned, and his texts are
about [men] possessions. Pallando Deceived: unique [isengard] condition, support area; at the start of the maneuver
phase its owner may exert a Wizard (Saruman, the only one, with no choice asked) and it is a fierce Wizard minion of
strength 10 and vitality 1 until the regroup phase, taking no wounds. The Free Peoples player assigns one to a
companion and the Shadow player the other; Aragorn 1_89 at Ettenmoors (8 + 2) is not overwhelmed by their 18, and
both win (`Card_13_078_Tests.TwoWizardsWhoWinOneSkirmishMakeItAMinionOnce`). The same pair is the Wizard minion who
is no Saruman (Ketura on Q-0311), of a second culture: a choice that wounds or exerts leaves Pallando out, as it
cannot take wounds and cannot exert at vitality 1 (`Card_17_020_Tests.AnIsengardWizardMinionWhoCannotTakeWoundsIsNotChosenBesideSaruman`).

**A minion with damage +2, to tell an X of 1 from 2.** Easterling Berserker 10_40. [raider] Man, strength 11,
vitality 3, cost 6, site 4, not unique. Damage +2 only while he is unwounded. "To play, spot an Easterling": cheat
him in. His Skirmish ability wounds 2 other [raider] Men, so with fewer than two beside him it cannot be paid.

**A [sauron] minion of cost 5, to read a discount of 3 above the floor of 0.** Morgul Hunter 1_256. [sauron] Orc, not
unique, cost 5, strength 9, vitality 4, site 6; "strength +1 for each companion you can spot" is its whole text.
At -3 it costs 2, and 2 more roaming on turn 1 (`Card_09_047_Tests`).

**A Shadow archer and nothing else.** Goblin Marksman 1_176. Strength 7, vitality 1, cost 3, site 4; Archer is its
whole text.

**An archer minion that lives through two archery wounds.** Orthanc Assassin 1_131. Unique [isengard] Uruk-hai,
cost 5, strength 10, vitality 3, site 5; "Archer. Damage +1" is its whole text (Goblin Marksman 1_176 has vitality
1).

**A Man minion of the opponent's, and nothing else.** Dunlending Brigand 4_10. [dunland] Man minion, strength 8,
vitality 1, no text: the valid candidate on the other side for a bare "a Man" (Q-0058). Beats a companion of
strength 5 to 7 without killing him. With 1 vitality he can never exert: where the Man must be able to ("exert 2 of
your Men"), the opponent's is Easterling Axeman 4_224 ([raider], strength 6, vitality 2; strength +3 and fierce
only at 2 burdens). The [dunland] Man who beats Aragorn or Elrond (8) is Dunlending Savage 4_17 (strength 11,
vitality 1, no text).

**A fierce minion with next to no other text (two of them, of other cultures and races, for "a minion" that loses
or keeps fierce).** Black Rider 12_161; White Hand Destroyer 17_124. Black Rider: [wraith] Nazgûl, not unique, cost
5, strength 8, vitality 3, site 3; strength +1 for each companion. Destroyer: unique [uruk-hai] Uruk-hai, cost 5,
strength 10, vitality 3, site 5; hunter 3 only for each site controlled. For a card that makes a [wraith] minion
fierce (the Q-0245 board, a candidate fierce already), Black Rider serves, or Úlairë Nelya 1_233: unique [wraith]
Nazgûl, cost 5, strength 10, vitality 3, site 2; her Shadow ability replaces a site and is used only on purpose
(Card_01_235_Tests).

**A hunter and nothing else, with a small number and a large one; a hunter who is no minion; a minion that is
damage +1 only while it bears a possession.** Tracking Uruk 15_171; White Hand Butcher 17_122; Faramir, Captain
of Ithilien 18_48; Uruk Decimator 12_150. Tracking Uruk: [uruk-hai] Uruk-hai, not unique, cost 5, strength 14,
vitality 3, site 5; Hunter 1 is its whole text. Butcher: [uruk-hai] Uruk-hai, not unique, cost 4, strength 5,
vitality 2, site 5; Hunter 6 is its whole text. Faramir: unique [gondor] Man companion, cost 3 (2 in a starting
fellowship), strength 7, vitality 3; ranger, hunter 2; a Maneuver heal of another companion. Decimator:
[uruk-hai] Uruk-hai, not unique, cost 4, strength 12 (+2 at a battleground), vitality 3, site 5; "While this
minion is bearing a possession, it is damage +1", and it has no keyword without one. In print (the
keyword lines of sets 1 to 19) no ally is a hunter and no Free Peoples character is fierce: "a hunter" that is no
minion is a companion, and "fierce, and no minion" has no candidate (`Card_15_170_Tests`).

**A minion whose own standing text gives it a keyword that is not fierce.** Desert Nomad 7_132. [raider] Man,
Southron, not unique, cost 4, strength 9, vitality 2, site 4: "While you can spot 2 threats, this minion is damage
+1". The damage-bonus twin of Easterling Axeman 4_224 (fierce at 2 burdens), for a keyword lost or barred "until" a
time while the minion's own text goes on giving it; a second copy is the control. Lay a second companion for the
two threats (`AddThreats(2)`), and read the bonus in the wounds of a lost skirmish (`Card_17_100_Tests`).

**A weak minion PRINTED fierce (a companion of strength 5 or 6 beats it or outlasts it in the fierce round).** Uruk
Pursuer 4_190; Uruk Seeker 4_195; Charging Uruk 15_156; Shelob, Eater of Light 8_25. Pursuer: [isengard] Uruk-hai,
not unique, cost 2, strength 5, vitality 3, site 5; Tracker; his texts are about unbound Hobbits (none may be
discarded; a response when one is killed). Seeker: the same with strength 6, vitality 2; strength +2 and damage +1
only against a bearer of a search card. Charging Uruk: [uruk-hai] Uruk-hai, not unique, cost 3, strength 7,
vitality 3; in a fierce skirmish strength +3 and hunter 1, so 11 against a non-hunter (the one to overwhelm a
Hobbit in the fierce round only). Shelob: unique [gollum] Spider, cost 6, strength 8, vitality 8, site 8; when
played she may play a [gollum] possession from the draw deck, so cheat her in: the fierce minion of another race
and culture, whom two fierce rounds do not kill. Crazed Hillman 12_60 ([men] Man, strength 7, vitality 1) is
strength +7 in a fierce skirmish. Card_08_113_Tests has all four.

**A minion that GAINS fierce: by its own ability, and by standing text.** Uruk Fighter 1_146; Easterling Axeman
4_224. Fighter ([isengard] Uruk-hai, strength 8, vitality 2, damage +1): "Maneuver: Spot 5 companions to make this
minion fierce until the regroup phase". Axeman ([raider] Man, strength 6, vitality 2): strength +3 and fierce while
2 burdens can be spotted, which outlasts the start of the regroup phase and so shows D for "cannot gain ... until
the regroup phase". Neither is unique: a second copy is the control (68_110). Any Uruk-hai, either culture, made
fierce by a printed card and not by hand: Hunt Them Down! 1_126, [isengard] Maneuver event, twilight 3, "Make an
Uruk-hai fierce until the regroup phase" (a choice of `uruk-hai`; one Uruk-hai is taken unasked): Card_01_147_Tests,
Card_01_150_Tests (dec-b1_146).

**A minion with ambush and nothing else; a second instance of ambush.** Southron Soldier 4_254; Raider Bow 7_155.
Soldier: [raider] Man, not unique, cost 2, strength 4, vitality 1, site 4; "Southron. Ambush (2)" is its whole
text. Bow: [raider] possession on a [raider] Man, "Bearer is an archer and ambush (5)": attach it after the archery
phase unless the archer is wanted (`mechanics/keywords/AmbushTests`; one offer for the sum, F-0010). Ambush printed outside
[raider]: Fearsome Dunlending 11_79 ([men] Man, cost 3, strength 9, vitality 2; ambush (1); a when-played offer at
a plains site only) and Warrior of Dunland 11_103 ([men] Man, cost 2, strength 8, vitality 1; ambush (1), and a
second ambush (1) at a plains site: site 2 of `FellowshipSites` is plains); Horror of Harad 14_13 ([orc]
Half-troll, unique, cost 4, strength 9, vitality 3) has it among damage +1, fierce, lurker, muster and toil 2.

**A muster character with no other text; two muster companions.** The Witch-king, Captain of the Nine Riders
11_226; Elven Scout 11_18. Witch-king: unique [wraith] Nazgûl, cost 8, vitality 4, site 3; Fierce, Toil 2 and
Muster are his whole text (fierce: two rounds with nobody assigned before the regroup phase). Elven Scout: [elven]
Elf companion, not unique, strength 6, vitality 3; "While you can spot another Elf, this companion has muster", so
two copies are two muster companions and nothing else.

**A toil card with no other text, and characters to exert for it.** Legion of Harad 11_88; Corps of Harad 11_73.
Legion: [men] Man, not unique, cost 6, strength 14, vitality 3, site 4; Toil 2 is its whole text. Corps: [men] Man,
not unique, cost 4, strength 10, vitality 3, site 4; fierce only at a battleground, and `FellowshipSites` has none.
Toil on a card played on a bearer: Berserker Torch 12_136 on Uruk Dominator 12_152 (F-0350).

**A twilight Nazgûl.** Úlairë Nelya, Ringwraith in Twilight 2_84. Strength 10, vitality 3, site number 2 (so not
roaming at site 2: the minion beside a roaming one); nothing else below 2 burdens and below 2 wounds on the
Ring-bearer (at either she is fierce and damage +1: on a board that wounds or exerts the Ring-bearer use Attëa).
Úlairë Attëa 2_82 is another, cheated in (strength 12, vitality 3; when played, may wound an ally); 2_83, 2_85 and
3_86 have optional "wins a skirmish" triggers. A Nazgûl neither twilight nor fierce: Úlairë Lemenya 1_232. Twilight
and no Nazgûl: Corpse Lights 6_99. Every twilight Nazgûl in print is [wraith] and every twilight [wraith] MINION is a
Nazgûl, so no minion tells `culture(wraith),twilight` from `twilight,nazgul`; the only twilight [wraith] cards that are
no Nazgûl are the conditions Cover of Darkness 103_64 and 103_65, which a spot could see and a wound cannot (dec-b2_76).

**The Balrog, for a card that plays "The Balrog".** The Balrog, The Terror of Khazad-dûm 12_79. [moria] Balrog,
cost 12, site number 4 (12 + 2 roaming at site 2), strength 17, vitality 5, damage +1; nothing is needed to play it
and nothing discards it; its text acts only at an underground site (fierce, takes no wounds, cannot be exerted).
The others get in a board's way: 2_51 discards every other minion, and itself when not underground; 2_52 (cost 14)
needs a [moria] Orc spotted and discards itself when not underground; 6_76 (cost 14) costs 3 less beside a [moria]
minion. Every minion titled The Balrog is [moria] (the sheet's culture `moria_balrog`, the Balrog icon, is
`Culture.MORIA`: 2_51, 2_52, 6_76, 12_79, 19_18). It is also the bearer for Whip of Many Thongs 2_74 or The
Balrog's Sword 2_50 (each "Bearer must be The Balrog") away from Moria.

**A besieger; a besieger who is no Orc; a card with the word in its title only; a legal way onto a controlled
site.** Gorgoroth Pillager 7_275; Great Hill Troll 8_102; Uruk Besieger 4_180; Gorgoroth Garrison 7_273. Every
besieger in print is a [sauron] minion. Pillager: Orc, cost 4, strength 10, vitality 2, site 5; in play its text
does nothing (it makes besiegers fierce only while it is stacked on a site its owner controls, so keep it off the
stack where fierce is read). Troll: cost 9, strength 16, site 5, printed fierce as every besieger Troll is (no use
where fierce is what is read); its optional trigger is for sites 5K to 9K only. Uruk Besieger: [isengard] Uruk-hai,
cost 2, site 5, damage +1, "Besieger" in the title and NOT the keyword: the invalid candidate for "besiegers"; his
own regroup action stacks him on a controlled site. Garrison: besieger Orc, cost 3, strength 9, site 5; two regroup
actions taken by text, "Stack a besieger" (discard 1 card from hand) and "Stack a non-besieger" (discard 2), each
stacking a [sauron] Orc from play on a controlled site.

**A tentacle that stays put.** Vile Tentacle 101_36. The only tentacle not discarded away from a marsh. Reaching
(19_19) and Strong (19_20) have nothing on being played; Foul (2_58) and Huge (2_66) have optional when-played
fetches, so cheat them in. No tentacle has more than 2 vitality. Away from a marsh the others are discarded: lay a
real marsh and never give a site the keyword (Q-0213: F). Midgewater Marshes (1_332; each Hobbit exerts on moving
there) goes in the site map as site 2, with a site after it that is no marsh; Moria Lake (1_346; Frodo or 2 other
companions exert on moving there) is the marsh of site 4. Neither has another keyword: where two marshes that differ in one are wanted, Watcher in the Water 19_21 ("The
fellowship's current site gains marsh"; it discards every other minion but tentacles) makes plains Ettenmoors 1_331
a marsh too (Card_02_058_Tests).

**A [sauron] Orc played by another card where his cost is read; an Orc who is not [sauron].** Orc Chieftain 1_266;
Morgul Hound 7_192. Chieftain: cost 2, strength 7, vitality 2, site 6; his text answers only a weapon played on
him. Not Orc Assassin 1_262 (cost 2, site 6): in play he makes the roaming penalty of each [sauron] minion you play
-1, so no cost is read beside him (stacked, he is the [sauron] Orc who is no besieger). Hound: [wraith] Orc, cost
2, strength 5, vitality 2, site 4; +4 and fierce only at 2 wounds on the Ring-bearer: also the [wraith] minion who
is no Nazgûl.

**A card that says minions are not roaming; a card that changes a site number.** Bill Ferny, Swarthy Sneering
Fellow 2_75; Grishnákh, Orc Captain 5_100. Bill: unique [wraith] Man, cost 2, vitality 1, site 2; "Nazgûl are not
roaming"; the Free Peoples player may not assign to him; discarded underground. Úlairë Lemenya 1_232 (Nazgûl, cost
4, site 3; her text needs a [wraith] condition on a companion) is the Nazgûl he frees at site 2. Grishnákh: unique
[sauron] Orc, cost 4, vitality 3, site 6, tracker; "The site number of each [sauron] Orc is -3", his own too; a
Shadow ability used only on purpose. Orc Chieftain 1_266 (cost 2, site 6) is then 3: roaming at site 2, not at site
3.

## Companions, allies and what they bear: plain ones, and ones for a keyword or a number

**A sturdy companion.** Aragorn, Ranger of the North 1_89. Strength 8, vitality 4; Ranger; a maneuver ability that
is only ever used on purpose.

**A weak companion.** Dwarf Guard 1_7. Strength 4, vitality 2; needs a Dwarf spotted to be played, so cheat it in.

**Hand weapons for a Dwarf, one or two.** Dwarven Axe 1_9; Hand Axe 2_10. Both [dwarven] possessions of cost 0
that a Dwarf bears. The Axe (strength +2) has a REQUIRED "each time a player's minion loses a skirmish to bearer":
the minion its bearer beats is still in play after `PassSkirmishActions()` until `FreepsResolveRuleFirst()`. The
Hand Axe (strength +1; an archery ability that discards it, used only on purpose) "may be borne in addition to 1
other hand weapon", so the two make a Dwarf's pair (lint-ok on the attach; Card_02_005_Tests).

**An archer, when archery is the point.** Legolas, Greenleaf 1_50. Strength 6, vitality 3; Archer, with an archery
ability.

**A Free Peoples archer in a skirmish, for a Shadow text's "if skirmishing an archer".** Companions: Legolas 1_50 and
Garrison of Gondor 8_36 ([gondor] Man, unique, strength 8, vitality 3; Knight, Archer; cheat it in past "spot 3
[gondor] companions"). An ally: Orophin 1_56 ([elven] Elf, unique, strength 3, vitality 3; Archer; Archery: exert him
to wound an Uruk-hai), made to skirmish by One Whom Men Would Follow 1_109 (strength 5 then); he then fires in
archery too, so only a minion with vitality to spare may be on the table before the assignment phase (Card_01_201_Tests).

**A Legolas who is no archer, for a text that names Legolas in a skirmish.** Legolas, Companion of the Ring 11_21.
Strength 6, vitality 3; his only text is +2 strength and resistance at a battleground or forest, which no site of
`FellowshipSites` is. An archer Legolas stops the skippers ("Archers get in the way"); 11_22 and 15_18 (hunter 1) are
the other non-archers (Card_01_033_Tests).

**A companion of another culture who is no archer, for "a [culture] companion wins (loses)".** Gimli, Son of
Glóin 1_13 ([dwarven] Dwarf, strength 6, vitality 3; a skirmish ability used only on purpose; his Damage +1 adds a
wound to a minion he beats). Legolas 1_51 in that place sets off an archery phase and stops the skippers ("Archers
get in the way"; Card_02_033_Tests, Card_02_034_Tests).

**A Free Peoples skirmish ability on a Ring-bound Man (Faramir 4_117), with no archer keyword.** Ranger's Bow 4_131:
[gondor] possession, cost 1, bearer must be a Ring-bound Man; "Skirmish: If bearer is skirmishing a Man or a roaming
minion, exert bearer to wound that minion." Faramir's Bow 4_118 does the same but makes him an archer ("Archers get
in the way"; Card_04_117_Tests).

**Elf companions who are no archers, for "spot N Elf companions".** Haldir 1_48 (strength 5, vitality 3; +2 at
sites 6 to 8 of the Fellowship path), Arwen 1_30 (strength 6, vitality 3; ranger, +3 against a Nazgûl), Lórien Elf
1_53 (strength 4, vitality 2). Haldir and Lórien Elf need an Elf spotted to play: cheat them in. Legolas 1_50's
archery wound can exhaust the minion under test before the skirmish ("Archers get in the way"; Card_01_261_Tests).

**Damage +1, when damage is the point.** Gimli, Son of Glóin 1_13. Strength 6, vitality 3; Damage +1, with a
skirmish ability. As the handy companion of another culture he gives a minion he beats 2 wounds: where one wound is
read, Household Guard 5_83 is the plain one.

**A damage bonus in print for "a Dwarf who is damage +X": a Dwarf ally with one and without; a companion with one
who is no Dwarf; a bonus that moves as he exerts.** Grimir, Dwarven Elder 1_17 bearing Ring of Guile 9_8; Huorn
9_25; Gimli, Counter of Foes 8_5. Grimir: unique Dwarf ally, strength 3, vitality 3, home 3F, no bonus of his own;
a fellowship ability only. Ring of Guile (Ring of Accretion 9_6 shares its first two sentences, not its Maneuver ability: no twin): unique [dwarven] artifact, cost 0,
"Bearer must be a Dwarf. Bearer is damage +1", and strength +1 in print, as every set 9 Dwarven Ring; its Maneuver ability discards it and is used only on purpose. Huorn:
[gandalf] Tree, not unique, strength 6, vitality 4, Damage +1; its Response needs an Ent. (Gandalf, Mithrandir 6_30
is a Wizard with Damage +1 who is wounded at every move.) Gimli 8_5: Damage +1 and +1 more for each wound on him,
so an exertion he pays changes it; never beside Gimli 1_13. A Dwarf ally who WINS a skirmish with a printed bonus:
Thrarin 1_27 (unique, strength 4, vitality 2, home 3F; his Maneuver ability is used only on purpose) bearing Ring
of Guile is 5 and damage +1 against Goblin Scrabbler 2_64 (4, vitality 1), at site 3 after `SkipToSite(2)` and a
pass (Card_10_003_Tests).

**A damage bonus given, and all of them taken away, by plain cards; a minion that lives through three wounds.**
Lord of Moria 1_21; Ranger's Sword 1_112; Lurtz's Battle Cry 1_128; Seek and Hide 3_112; Gollum, Nasty Treacherous
Creature 5_24. Lord of Moria (support-area condition): "Each Dwarf is damage +1". Ranger's Sword (strength +2, on
Aragorn): "He is damage +1" and nothing else. Battle Cry ([isengard] Skirmish event, cost 1): an Uruk-hai is
strength -1 and damage +1. Seek and Hide ([shire] Skirmish event, cost 1, spot 2 [shire] companions): a minion
loses all damage bonuses. Gollum (strength 5, vitality 4; a regroup ability used only on purpose) loses to Gimli
1_13 and shows 1, 2 or 3 wounds (mechanics/keywords/DamageBonusTests).

**A companion's damage bonus taken away, with no event played (the `-` of "while he is damage +X").** Saruman, Mind
of Metal and Wheels 102_29 with Isengard Worker 3_62. Saruman: unique [isengard] Wizard minion, "Companions
skirmishing [isengard] Orcs lose all damage bonuses"; he may not be assigned, and his Response (exert him to
prevent a wound to an [isengard] Orc) is used only on purpose. Worker: [isengard] Orc, strength 5, vitality 3; a
regroup ability only. Gimli 9_4 assigned to the Worker is damage +1; in the skirmish, 0 (Card_09_004_Tests).

**A companion who loses to a minion with damage +1 and takes one wound.** Mithril-coat, Dwarf-mail 13_153. [shire]
artifact (armor), unique, cost 2; bearer must be a Ring-bound Hobbit (Sam 1_311). Each minion skirmishing bearer
loses all damage bonuses. Every plain Uruk-hai has Damage +1, which gives the loser two wounds and can hide "not
overwhelmed" behind a death. Its skirmish ability (exert bearer: those minions lose fierce) is used only on
purpose.

**A companion of 6 vitality by legal cards.** Aragorn 1_89 with The Saga of Elendil 1_114 and Ranger's Cloak 15_68.
Vitality +1 each. Saga: unique [gondor] tale, a condition a [gondor] companion bears; "Skirmish: Discard this
condition to make bearer strength +2", used only on purpose. Cloak: [gondor] possession on a [gondor] Man,
resistance +1 as well; while bearer is a ranger (Aragorn 1_89 is) each minion skirmishing him is site number +2,
which matters only where roaming or a site number does.

**A Hobbit companion at strength 0 by cards in play, with no event played.** Saruman's Frost 1_135; Isengard Sword
11_192 on White Hand Destroyer 17_124. Frost: [isengard] condition played on a site, limit 1 per site (to play,
exert an [isengard] minion: attach it to the current site); each Hobbit at that site is strength -2: companions
and Hobbit minions while the fellowship is there, a Hobbit ally at his home site wherever the fellowship is
(F-0532); discarded at the end of the turn. Sword:
[uruk-hai] hand weapon on an [uruk-hai] minion, "Each character skirmishing bearer is strength -2". Destroyer:
unique [uruk-hai] Uruk-hai, strength 10, vitality 3, fierce, no damage bonus. Sam 1_311 skirmishing it reads 0 (3 -
2 - 2): overwhelmed by a side of any strength (CR 4.2 "overwhelmed"), and one wound where he is not
(Card_10_116_Tests).

**Defender +1 by legal play.** Aragorn, Ranger of the North 1_89; Disquiet of Our People 2_2. Aragorn: "Maneuver:
Exert Aragorn to make him defender +1 until the regroup phase." Disquiet: [dwarven] Maneuver event, cost 1, "Exert
a Dwarf to make that Dwarf defender +1 (or defender +2 if you spot an Orc) until the regroup phase": beside Goblin
Patrol Troops (Orcs) it is +2.

**An item that changes no number.** Dwarven Armor 1_8. Possession, cost 0, bearer must be a Dwarf; bearer may not
be overwhelmed unless strength is tripled. The ARTIFACT for a Dwarf that changes no strength: Ring of Artifice 17_4
(unique Ring, cost 0; damage +1, and strength +1 for each [dwarven] artifact in its owner's discard pile, so none
while that pile holds none; its Regroup ability discards it). Not a set 9 Dwarven Ring: 9_6 to 9_9 each print strength +1.

**A follower that changes no number.** Rohan Worker 15_135. Not unique, no printed strength or resistance (most
followers give the bearer one); Aid (1); its texts answer a transfer to a companion who is not [rohan] (bearer
exerts twice: cheat it on) and a skirmish its bearer wins. Elendil's Army 18_47 (unique, strength +2, no
resistance; Aid: exert a [gondor] companion) is the one to transfer by legal play: see "A follower's Aid" in the
pitfalls.

**A follower with a plain bonus.** Pallando, Far-travelling One 13_37. Unique [gandalf] follower, cost 2, strength
+1, Aid (2). Transferred to a companion who is not [gandalf] he exerts the bearer twice, so Gandalf 1_72 bears him
(`mechanics/keywords/AidTests`); his other trigger needs the bearer to win a skirmish. Also the [gandalf] Free Peoples card
that is no character: the candidate left out by "a [gandalf] character" (Card_01_087_Tests, in the draw deck).

**A valiant companion and nothing else.** Household Guard 5_83. [rohan] Man, strength 6, vitality 3; Valiant is its
whole text. Every card PRINTED valiant is a companion, and no ally is given the keyword by hand (Q-0213: C); but a
game reaches a valiant ally by two printed cards, so "valiant companion" is told from "valiant" with those ("A
valiant ally").

**A valiant ally (no ally is valiant in print; beside "each valiant companion"); a valiant Ring-bound companion.**
Northern Signal-fire, Flame of Calenhad 103_86; Royal Guard 102_54; Erkenbrand, Kinsman of the House of Eorl
102_47. Flame ([rohan] possession, Beacon, Support Area; to play, hinder 2 beacons: cheat it into the support
area): "Maneuver: Hinder this beacon and exert an unbound Man. Until the regroup phase, each of your other Men gain
each of the unloaded keywords (except race) on that Man". With Household Guard 5_83 the only unbound Man he exerts
with nobody asked (with two unbound Men, choose the valiant one), and a Man ally (Guma 4_277) is valiant until the
regroup phase. EVERY other Man of the player's gets the keyword, so the companions who must stay not valiant are no
Men (Gandalf): `Card_V2_014_Tests.AnAllyWhoIsValiantIsNotMadeStronger` is the board that disturbs nobody else's
reading. Royal Guard ([rohan] Man ally, unique, cost 2, strength 3, vitality 3, home 3T; to play, spot Théoden or 3
[rohan] Men: cheat him in): "Skirmish: Exert this ally to add his strength to a skirmishing Hobbit and make that
Hobbit gain valiant until the regroup phase (limit once per phase)", so a Hobbit ally (Bounder 1_286 at site 2 of
`FellowshipSites`) is valiant in and after his own skirmish, and nobody else is touched. Erkenbrand ([rohan] Man
companion, unique, cost 5, strength 7, vitality 3, valiant): "Each exhausted companion gains valiant", so Sam 1_311
with 3 wounds is a valiant Ring-bound companion (his when-played text removes [rohan] companions in the dead pile
from the game: cheat him in). By hand the keyword is still given to no ally (pitfalls, "Making a card something it
is not").

**Companions by signet.** Arwen, Daughter of Elrond 1_30; Boromir, Lord of Gondor 1_96; Merry, From O'er the
Brandywine 1_303. The Aragorn signet in three cultures ([elven], [gondor], [shire]). Gandalf signet: Gimli 1_13,
Aragorn 1_89 (Gandalf, Friend of the Shirefolk 1_72 has the Frodo signet: he is the Gandalf to spot and the
candidate not offered). Frodo signet: Legolas 1_50 and Frodo 1_290 himself, whose own fellowship ability exerts
another companion of that signet to heal him, so another card's fellowship action beside him is read and taken by
card. No signet: Haldir 1_48 and Lórien Elf 1_53 (each needs an Elf spotted: cheat them in), and Dwarf Guard 1_7 ("To
play, spot a Dwarf": as a not-offered candidate in hand, lay the Dwarf ally Grimir 1_17 and read the Guard
playable, or its own text refuses it; Card_01_364_Tests). Théoden signet: its
readers (I Am Here 5_84, Forth Eorlingas! 4_275, No Living Man 8_90) each exert Théoden; the Théoden who can exert
twice and has not the signet himself is Tall and Proud 8_92 (vitality 3, Aragorn signet; the three of sets 4 and 5
have vitality 2). Horn of the Mark 9_45 gives it to Merry or Pippin beside his printed signet. `packet.py --find`
prints a signet at the end of the id line ("signet Frodo"). A possession and an artifact on one Gandalf: Glamdring 1_75
(hand weapon, cost 2) and Gandalf's Staff 2_22 (Staff, cost 2); Wizard Staff 2_29 is a Staff too, so beside Gandalf's
Staff it is not offered to him (one item of a class; met while building Card_02_023_Tests, which lays the pair).

**A Ring-bound companion who is not the Ring-bearer.** Sam, Son of Hamfast 1_311. Strength 3, vitality 4;
Ring-bound by rule. A fellowship ability that is only ever used on purpose, and a response when Frodo dies. Without
him "the Ring-bearer" written as `ring-bound` passes every test.

**A Ring-bearer who is not Frodo.** `VirtualTableScenario.GimliRB` (Gimli, Bearer of Grudges 9_4, a Dwarf),
`GaladrielRB` (9_14, an Elf), `SmeagolRB` (9_30) or `SamRB` (13_156, a Hobbit) as the scenario's Ring-bearer; a
cancel of a skirmish involving one race owes its `FormatRule` pair on the Ring-bearer of that race (Voice of
Nimrodel 3_28: Galadriel 9_14 under `fotr_block` and `multipath`). And NO Frodo beside him: no Frodo who may bear the Ring
is ever in play beside another Ring-bearer (CR 4.2 "Ring-bearer"; Q-0159: B). The one Frodo who can stand there is
Frodo, Frenzied Fighter 13_149 ("This version of Frodo cannot become the Ring-bearer", CR 4.2; [shire] Hobbit,
Ring-bound, strength 3, vitality 4, resistance 8; he cannot be overwhelmed short of triple while minions outnumber
companions, and has a skirmish ability used only on purpose; Card_V1_017_Tests plays him from hand beside Gimli):
he is laid only where the text under test names Frodo and a Frodo who is not the Ring-bearer is what the test is
about (Sting 51_313; "If Frodo dies", CR 4.2 glossary "Sam", 51_311), never as one more candidate for "the
Ring-bearer" (Q-0212). For "the Ring-bearer" a `name(Frodo)` filter then finds nobody (Card_94_026_Tests); where
the Ring-bearer is the EXCLUDED candidate ("a companion (except the Ring-bearer)"), `GimliRB` with no other
companion has nothing offered, and `not(name(Frodo))` would offer Gimli (Card_93_003_Tests); where the test needs 4
or more burdens, Sam, Bearer of Great Need 13_156 (resistance 5), since Galadriel corrupts at 4. Bilbo, Bearer of
Things Burgled 9_49 (given as the string; resistance 8) asks "add 2 burdens or add (3)" at every move made with 2
or fewer in the pool: `SetTwilight(3)` in the fellowship phase before a skipper moves him (Card_02_108_Tests). For
the test that needs one: a text that names Frodo, a Ring-bearer's own text. No longer asked for on every card that
says "the Ring-bearer" (Q-0213: A: `name(Frodo)` has never been written for him); the tests that have one stay.

**A Hobbit ally who may be assigned on turn 1.** Bounder 1_286. [shire] Hobbit ally, not unique, cost 1, strength
2, vitality 2, home 2F (site 2 of `FellowshipSites`); a skirmish ability used only on purpose. The Hobbit who is no
companion. At his home site either player may assign him with no card's help (CR 4.2 "ally"; pitfalls, "An ally in
a skirmish").

**An ally who bears a weapon, or three cards.** Farmer Maggot, Chaser of Rascals 1_288; Bilbo, Retired Adventurer
1_284; with Hobbit Sword 1_299, Orc Armor 10_113, Salt from the Shire 11_171. A Hobbit ally lawfully bears whatever
says "Bearer must be a Hobbit", so with one in play beside Frodo (Bounder 1_286) the bearer of such a card is
asked, not chosen unasked (Card_04_343_Tests). Maggot: unique [shire] Hobbit ally, cost 1, strength 2, vitality 3, home 1F;
"Fellowship: Exert Farmer Maggot to heal Merry or Pippin". Bilbo: the same at cost 2, home 3F; "Fellowship: Exert
Bilbo to shuffle a [shire] card from your discard pile into your draw deck". Both abilities are used only on
purpose. Hobbit Sword: [shire] hand weapon, cost 1, strength +2, no other text. Orc Armor: [shire] armor, cost 1;
each Orc and each Uruk-hai skirmishing bearer is strength -1. Salt from the Shire: [shire] box, cost 1, resistance
+2; two abilities that discard it, used only on purpose. With a pipe (Bilbo's Pipe 1_285) that is four classes,
which is how an ally comes to bear a weapon, or three cards of different classes.
An ally who bears a weapon with a DAMAGE bonus, and can skirmish: Thrarin, Dwarven Smith 1_27 (unique [dwarven]
Dwarf ally, strength 4, vitality 2, home 3F; "Maneuver: Exert Thrarin to allow him to participate in archery fire
and skirmishes until the regroup phase") bearing Gimli's Battle Axe, Trusted Weapon 7_9 (unique hand weapon, any
Dwarf may bear it, strength +2; damage +1 while a threat can be spotted, so lay one; Card_03_072_Tests).

**A Bilbo of each card type, for a text that names Bilbo.** The ally Bilbo, Retired Adventurer 1_284 (home 3F;
above); the companion Bilbo, Melancholy Hobbit 12_119 ([shire] Hobbit, Ring-bound, strength 3, vitality 4); Bilbo,
Bearer of Things Burgled 9_49 as Ring-bearer ("A Ring-bearer who is not Frodo"). One title: one at a time. 12_119's
"At the start of each skirmish involving Bilbo, you may exert him to ..." has a cost, so it is offered in each of his
skirmishes whatever is in the discard pile: decline it (`FreepsDeclineOptionalTrigger()`) right after
`FreepsResolveSkirmish`, or the next pass is refused (Card_02_097_Tests).

**A Hobbit bearing a weapon of each item type, and one bearing an item that is none.** Hobbit Sword 1_299 (a
possession) on Sam 1_311; Sting 30_47 (Hobbit set: the one ARTIFACT weapon a Hobbit can bear, owed under Q-0312;
`target: name(Bilbo)`, so on Bilbo 1_284; unique title shared with Sting 1_313); the opponent's Hobbit Ted Sandyman
18_75 bearing Poleaxe 11_95 ([men] hand weapon, strength +2; its skirmish ability used only on purpose). The
invalid one is Frodo bearing The Gaffer's Pipe 1_292 (a Pipe possession, "Bearer must be a Hobbit"; its fellowship
ability not reached). The Ruling Ring is NOT that card: its type is The One Ring, no item, so `hasAttached(item)`
passes it and only the Pipe tells `weapon` from `item` (Card_02_103_Tests).

**A [rohan] Man who is an ally (and a second).** Guma, Plains Farmer 4_277; Hlafwine, Village Farmhand 4_281. Each:
unique, cost 1, strength 4, vitality 2, home 4T (never fighting on a Fellowship path), Villager; discarded if an
opponent controls his home site. Guma: "Fellowship: Exert Guma to heal a [rohan] Man"; Hlafwine: "Skirmish: Exert
Hlafwine to make a [rohan] Man strength +2"; both used only on purpose. Not unique, and with no "discarded" text:
Rohirrim Herdsman 7_247 (cost 1, strength 4, vitality 2, home 3T, Villager; "To play, spot 2 [rohan] Men", which
`MoveAlliesToTable` passes over; a regroup ability used only on purpose). On the Towers path his home is Barrows of
Edoras 4_337, a sanctuary; with Westemnet Plains 4_342 as site 3 it is a plains (Card_05_094_Tests).

**An ally whose home is site 3 or 6 of a block other than the Fellowship's (beside "an ally whose home is site
6").** Aldor, Soldier of Edoras 4_262. [rohan] Man ally, unique, cost 1, strength 3, vitality 2, homes 3T and 6T;
"Archery: Exert Aldor and spot a villager to make the fellowship archery total +1" (it requires a villager to
spot). The printed card for this, so no Hobbit-set ally (Esgaroth Volunteers 32_17, home 6H) is wanted: only he
notices `allyHome(T,6)` let in beside `allyHome(fellowship,6)` (Card_01_045_Tests). At a 6T site (Great Hall
4_353) he is at home whatever the site's text says, so he is no candidate for a text that lets allies take part
"at" that site: `allyHome(T,6)` in place of its filter would pass with him (Card_04_353_Tests, dec-b4_353).
For an ELF ally of such a site 3, Aldor fails "Elf" as well: the only printed one is Rivendell Scout 33_16 (Hobbit
set, home 3H; unique Elf archer, strength 6, vitality 3; an optional trigger on a companion healed), laid under
Q-0312 as the one card that tells `allyHome(fellowship,3)` from a home 3 of any block (Card_01_064_Tests). No Elf
ally has a home 6 outside the Fellowship block, so an "Elf ally whose home is site 6" has no such board (1_65).

**A [gondor] character who is not a companion; an ally who is a Man.** Gondorian Merchant 7_97. The only [gondor]
ally; a unique Man, strength 6, vitality 1; home 3T, so he skirmishes on the Towers sites (`SkipToSite(2)` and a
move). His maneuver ability needs initiative and 2 cards discarded from hand: only ever used on purpose. The ally
among the candidates for "a character" or "a Man" (he may bear Flaming Brand 2_32). He is not out of reach of an
exertion or a vitality threshold: Narsil 9_34 (hand weapon) and Ranger's Cloak 15_68 (cloak), each vitality +1 and
"Bearer must be a [gondor] Man", make him 2 or 3 (Card_09_038_Tests, Card_09_046_Tests); for "exert bearer twice"
on a hand weapon (Ithilien Blade 15_62, so no Narsil) +2 vitality by hand (Card_15_062_Tests), which is what notices a
`companion` added to "a [gondor] Man".

**An ally in a skirmish away from any home site (a Shadows-block path).** One Whom Men Would Follow 1_109. [gondor]
Maneuver event, cost 0: exert Aragorn (any Aragorn; he keeps the wound), and a chosen ally is strength +2 and takes
part in archery fire and skirmishes until the regroup phase. Nothing else; the ally is then assigned like a
companion. A Wizard who is an ally and WINS a skirmish is Radagast, The Brown 31_17 by this event: 8 + 2 beats Uruk
Shaman 1_152 (8, vitality 2) without killing it (`Card_13_078_Tests.AWizardWhoIsAnAllyAndWinsASkirmishMakesItAMinion`).
His 10 overwhelms a minion of strength 5 or less (Uruk Savage 1_151 is discarded, not wounded): for a wound to
read, lay a minion of strength 6 to 9; or read the kill (`Card_11_244_Tests`, dec-b11_244).
`FreepsCanAssign(ally)` reads his taking part in any phase: false before the event, true after it, false again at
the first regroup decision (`Card_01_109_Tests`).
On the Towers path no Hobbit ally is at home (each one's home is F or H), and this event is how one skirmishes there:
Bounder 1_286 with Aragorn in play, in the maneuver phase (`Card_04_321_Tests`, for "a skirmish involving a Hobbit").

**An ally in a skirmish at any site, with no Aragorn needed.** Thrarin, Dwarven Smith 1_27: unique [dwarven] Dwarf
ally, strength 4, vitality 2, home 3F. "Maneuver: Exert Thrarin to allow him to participate in archery fire and
skirmishes until the regroup phase." Used, he is assigned like a companion; Goblin Patrol Troop 1_177 (13)
overwhelms him, for "an ally killed" (Card_07_363_Tests).

**A special ability of an ally in a phase that has minions.** Rúmil, Elven Protector 1_57; Calaglin, Elf of Lórien
3_10; Albert Dreary, Entertainer from Bree 1_69. Each unique, vitality 3, and used away from home. Rúmil (Elf,
archer, home 6F): "Archery: Exert Rúmil to wound an Orc". Calaglin (Elf, archer, home 6F): "Regroup: Exert Calaglin
to wound a minion". Albert (Man, home 1F; to play, spot Gandalf): "Maneuver: Exert Albert Dreary to discard an
[isengard] or [moria] condition". With one Orc on the table Rúmil's wound is asked of nobody. Rúmil is also the
second Elf archer for "spot 2 Elf archers" (every Elf companion printed an archer is a Legolas; an ally away from
home is spotted and adds nothing to the archery total: Card_13_021_Tests).

**An artifact of a culture other than [shire] that a Hobbit can bear, with a cost above 0.** Phial of Galadriel,
Bane of Darkness 103_1: unique [elven] artifact (phial), cost 1, vitality +1, "Bearer must be a Ring-bound Hobbit"
(Frodo or Sam 1_311). It discards itself while 5 companions can be spotted, so lay 4 or fewer; its Maneuver ability
(2 burdens) and its Ring-bearer responses are used only on purpose. The other Phials, 10_13 and 53_24, cost 0 and
show no discount (Card_02_102_Tests, "each artifact").

**A Free Peoples shield, armor or hand weapon of cost 2, to read a discount of 1 (a cost never reads below 0).**
Mithril-coat 2_105 (unique [shire] artifact armor, "Bearer must be Frodo"; archery total -1, no weapon bonuses for
minions skirmishing him), Glamdring 1_75 (unique [gandalf] possession hand weapon, on Gandalf; damage +1, a
fellowship ability used only on purpose), Sturdy Shield 15_141 ([rohan] possession, on a [rohan] Man). No helm in
print costs more than 1 (Card_02_025_Tests).

**Seven [elven] artifacts in play at once, for a count that stops at +6 ("(limit +6)", Aiglos 9_12).** Each unique,
of its own title. Aiglos 9_12 and Vilya 9_23 (cost 0, "Bearer must be Elrond or Gil-galad") on Gil-galad 9_15;
Phial of Galadriel, Star-glass 10_13 (cost 0, on a Hobbit: the Ring-bearer); in the support area Library of
Rivendell 101_11, Galadriel's Silver Ewer 18_11 (its bonuses are for Galadriel as Ring-bearer), The Mirror of
Galadriel 15_22 (to play, spot 3 Elves: cheat it in; answers a draw) and Gift of the Evenstar 15_16 (answers a won
skirmish and the start of a maneuver phase). Their abilities are used only on purpose (`Card_09_012_Tests`).

**The title half of "Elrond (or 2 [elven] allies)" alone (Master of Healing 3_22).** Elrond, Lord of Rivendell 1_40
is himself an [elven] ally, so with one more [elven] ally beside him the two-allies half is met too and the title
is never what lets the card be played (`name(Elrond),companion` survived such a board, dec-b3_18). Lay him with no
other [elven] ally (Albert Dreary 1_69, a [gandalf] ally, may stand beside him); the Elrond who is a companion is
Elrond, Witness to History 12_17 (`Card_03_022_Tests`).

**Cards for "play a <culture> artifact (condition, possession) from your draw deck", one for each place it goes.**
[elven] artifacts: The Mirror of Galadriel, Dangerous Guide 15_22 (unique, cost 2, to the support area; to play,
spot 3 Elves, an Elf ally among them: Orophin 1_56) and Phial of Galadriel, Star-glass 10_13 (unique, cost 0,
"Bearer must be a Hobbit": the Ring-bearer); Elven Bow 1_41 (an [elven] possession) and Ring of Guile 9_8 (a
[dwarven] artifact) are the ones not offered (`Card_18_007_Tests`). [men] cards with no requirement: Lying in Wait
13_95 (condition, cost 0, support area), War Trident 12_77 (possession, cost 1, "Bearer must be a [men] minion") and
Fleet of Corsair Ships 18_66 (possession, cost 3, support area; its play has a "you may add a [men] token"
trigger); Man of Bree 11_90 is the [men] minion not offered (`Card_12_073_Tests`).

**A [rohan] mount whose text reaches no test (to make a [rohan] Man "mounted", or a mount on Éomer).** Firefoot,
Mearas of The Mark 13_126 and Snowmane, Noble Mearas 13_136: unique, cost 2, strength +1 (Snowmane resistance +1 as
well), "Bearer must be a [rohan] Man". Each play trigger reinforces a [rohan] token and is withheld with none in
play; 13_126's maneuver trigger and 13_136's skirmish ability need Éomer or Théoden and 2 [rohan] tokens. 13_126 is
titled Firefoot, so it is never on a board with Firefoot 4_274 (`Card_04_268_Tests`).

## A culture without its race, a race without its culture, a card against its type

**A culture without its race, or a race without its culture: a printed card, never a modifier (Q-0213: B).** By
pair: Sméagol, Once Like Us 103_116, Goldberry 9_51, Tom Bombadil 9_52; Pippin, Trollslayer 103_31, Merry, Master
Holbytla 103_85, Ted Sandyman 18_75; King of the Dead 8_38; Khamûl 103_61, Morgul Creeper 8_75; Saruman 17_116,
4_33, 3_68, Cirith Ungol Guard 10_81, Half-troll of Far Harad 14_12; Ent Avenger 6_27; Corpse Lights 6_99. [shire]
and no Hobbit: Sméagol 103_116 (companion), Goldberry 9_51, Tom Bombadil 9_52 (allies). A Hobbit who is not
[shire]: Pippin 103_31 ([gondor]), Merry 103_85 ([rohan]), Ted Sandyman 18_75 (a [men] minion). [rohan] and no Man:
Merry 103_85 alone, and he is a Man while 3 [rohan] companions or Éowyn can be spotted (so a text that spots or
counts 3 or more [rohan] companions cannot use him: Into the Caves 17_100). [gondor] and no Man: Pippin
103_31, and the Wraiths of set 8 (King of the Dead 8_38: to play, spot a [gondor] Wraith and add 2 threats, so
cheat him in). A Nazgûl who is not [ringwraith]: Khamûl 103_61 (an Easterling too; not twilight); [ringwraith] and
no Nazgûl: Morgul Creeper 8_75 (strength 8), or Morgul Hound 7_192 (an Orc of strength 5 and vitality 2, so it can
exert, and it beats Frodo 1_290 with The Ruling Ring, 3 + 1, by one; +4 and fierce only at 2 wounds on the
Ring-bearer). [uruk-hai] and no Uruk-hai: Saruman 17_116; an Uruk-hai of [sauron]: Cirith Ungol Guard 10_81. [orc]
and no Orc: Half-troll 14_12. [dunland] and no Man: Saruman, Rabble-rouser 4_33 alone, who "may not be assigned to
a skirmish" until he bears Saruman's Staff 4_174 and its maneuver ability is used (lay him after the move: "Each
time the fellowship moves, the Free Peoples player must exert a companion for each [dunland] Man you can spot");
then (strength 10, fierce, damage +1) he is the invalid candidate for a [dunland] Man who wins, loses or is in a
skirmish (Card_04_032_Tests). His text has no bar on wounds (3_68, 3_69, 4_173 and 5_56 have one), so with no
Staff, laid after the move, he is the invalid candidate for a bar on wounds to [dunland] Men (Card_04_342_Tests). [gandalf] and no Wizard: Ent Avenger 6_27 and the [gandalf] Men ("A [gandalf] Man").
A Wizard or a Wraith who is not [gandalf] or [gondor] is on the Shadow side only (Saruman, Keeper of Isengard 3_68;
Corpse Lights 6_99, a [sauron] Wraith), for a text that names no side. NONE in print, and so never asked for: an
Elf outside [elven] or an [elven] character who is no Elf (141 of each, the same 141); the same for Dwarf and
[dwarven] (53); a Free Peoples Wizard outside [gandalf]; a Free Peoples Wraith outside [gondor]. `lint.py` reports
a definition that goes by the one where its text names the other (`race-culture`): a finding to file. Those are
CHARACTERS: for "spot a Dwarf", whose candidates need not be characters, a [dwarven] condition is the printed
invalid one (Lord of Moria 1_21 in the support area; Card_01_011_Tests, Card_02_006_Tests).

**An Aragorn who is not [gondor].** Aragorn, Defender of Rohan 17_93. The only Aragorn of another culture
([rohan]), and no signet; his text does nothing unless the Ring-bearer is assigned to a skirmish. Without him a
`name(Aragorn)` filter narrowed to [gondor] passes every test. Aragorn signet: 1_365 (optional start-of-turn
trigger, so put him down after `StartGame()`), 4_364. Gandalf signet: 1_89, 4_109. There is no Boromir outside
[gondor].

**A Saruman who is no minion (the follower, for a bare "Saruman" crossing type, Q-0306).** Saruman, Servant of
Sauron 17_38: unique [isengard] follower, cost 1, Aid - (1); a skirmish-win trigger only. A site's "Play Saruman
from your discard pile; his twilight cost is -2" plays him to the support area for 0 (Orthanc Balcony 4_361,
`Card_04_361_Tests.PlaysTheSarumanFollowerFromTheDiscardPileAtNoCost`). His Aid is offered as the maneuver phase starts.

**A Hobbit who is not [shire].** Pippin, Trollslayer 103_31; Merry, Master Holbytla 103_85. The only two Free
Peoples ones. Pippin is [gondor] (to play, spot a [gondor] companion: cheat him in; a skirmish ability used only on
purpose); he is Enduring, strength +2 for each wound on him, so leave him out of a test that reads his strength
after he is wounded (a "until the regroup phase" bonus read after a lost skirmish, Card_01_317_Tests). Merry is [rohan] (the only [rohan] character who is no Man), and a Man as well while 3 [rohan] companions
or Éowyn can be spotted (he counts himself among the 3; beside any Éowyn he is the Hobbit of the wrong culture and
the Man of the wrong culture at once); each time another [rohan] Man exerts he offers an optional trigger (exert
Merry to heal that Man), at any exertion, a cost included (an ally's own "Exert Hlafwine to ...", 4_281), and the
offer comes BEFORE that ability's choice of target; so keep him out of a test that exerts one, or, where he is the invalid candidate beside an
"exert a [rohan] Man" cost, decline his offer before going on (Card_11_259_Tests). He can hold an item only a [rohan] Man may bear
while he is NO Man: lay him beside Éowyn with it, and have her killed in an earlier skirmish of the same turn; he
keeps the item and is a Hobbit only (`Card_06_091_Tests.AMountOnMerryPaysOnlyWhileHeIsARohanMan`). Without one, "a
Hobbit" narrowed to [shire] passes every test. The opponent's Hobbit, for a bare "Hobbit" (Q-0058), is Ted Sandyman
18_75: a [men] MINION whose race is Hobbit (strength 3, vitality 4, site 2; a maneuver ability that needs a site he
controls and a regroup ability used only on purpose). He bears the [men] minions' weapons: Henchman's Dagger 18_69
(hand weapon, cost 0, strength +1) and Long Battle Bow 11_89 (ranged weapon, strength +1, archer: see "Archers get
in the way").

**A [shire] character who is no Hobbit.** Goldberry, River-daughter 9_51. Ally, home 2F, strength 4, vitality 6, no
race; her texts need a river. Tom Bombadil 9_52 is the other ally (to play, remove 2 burdens or 2 threats: cheat
him in). The one COMPANION is Smeagol, Once Like Us 103_116: strength 3, vitality 4, resistance 6, Ring-bound, no
race; to play, add a burden (cheat him in); an assignment ability only. He can fight and bear a follower. His
title is "Smeagol" with no accent, and `name(Sméagol)` takes him, as `Filters.name` compares sanitized titles
(Card_12_040_Tests).

**Each Gollum and each Sméagol, by culture; a [gollum] minion of another title** ("spot Gollum or Sméagol", "a
[gollum] minion"). Gollum, Nasty Treacherous Creature 5_24 ([gollum] minion, cost 2, strength 5, vitality 4; a
regroup ability only) and Gollum, Half a Wraith Himself 103_66 ([wraith]); Sméagol, Poor Creature 6_45 ([gollum]
companion, Ring-bound; to play, add a burden; a skirmish ability used only on purpose) and Smeagol, Once Like Us
103_116 ([shire], above). Shelob, Last Child of Ungoliant 8_26 ([gollum] minion, cost 6, enduring, fierce, damage
+1 in a fierce skirmish) is the [gollum] minion who is no Gollum: beside Gollum alone `name(Gollum)` passes for
`culture(gollum),minion` (Card_15_051_Tests).

**A [gandalf] Wizard who is an ally; a weapon that is no possession.** Saruman, The White 32_24; Glamdring (The
Hobbit) 30_29. Saruman is an ally (home 3H, cost 4, strength 8, vitality 4, wise); a Gandalf is defender +1 beside
him, and his two abilities are used only on purpose. Without him "a [gandalf] Wizard" narrowed to companions passes
every test (67_15, 67_24); Radagast, The Brown 31_17 is the other (home 5H, cost 4, strength 8, vitality 4, wise;
his fellowship ability plays a Gandalf from the discard pile and is used only on purpose). For a Shadow card's bare
"a Wizard" ("exert a Wizard", "Bearer must be a Wizard") the candidates are the Shadow player's Saruman, the other
player's Gandalf and this ally, and all three are offered (CR 4.2 "Shadow phase(s)", Q-0058; `Card_13_079_Tests`,
and Staff of Saruman 13_81 in `Card_13_080_Tests`). Glamdring 30_29 is a [gandalf] artifact hand weapon
(strength +2, on Gandalf by name; an optional trigger when he wins a skirmish): it tells `weapon` from
`possession`, and shares its unique title with Glamdring 1_75, so never both on one board. Orcrist 30_17 is the one
[dwarven] weapon that is no possession (an artifact hand weapon, strength +2, damage +1, on Thorin by name: Thorin,
Oakenshield 30_19), laid under Q-0312 for a [dwarven] text that says "weapon" (Card_02_001_Tests).

**A Wizard in a dead pile or discard pile who cannot be played (the `C-` of "play a Wizard / Gandalf from your dead
pile", Q-0062).** Gandalf, Powerful Guide 15_29. Unique [gandalf] Wizard companion, cost 4, strength 8, vitality 4,
resistance 7: "To play, spot 2 Hobbits", the one printed Free Peoples Wizard with a requirement to play. Beside one
Hobbit he lies in the pile and the offer is not made; beside two it is (Card_11_255_Tests). His two optional
triggers (a spell played: add a burden to wound a minion; the start of his own skirmish: draw and discard) come up
only once he is in play.

**A [gandalf] companion who is not Gandalf; a second Ent.** Ent Avenger 6_27. Strength 6, vitality 4, not unique; a
strength bonus only when 3 Ents can be spotted. Tells `name(Gandalf)` from `culture(gandalf)`. The Ent of another
title and type beside the ally Treebeard, Earthborn 4_103 (vitality 4, unhasty; a response and a fellowship ability
about unbound Hobbits stacked on him).

**A [gandalf] Man (Ent Avenger 6_27 is the [gandalf] companion who is none).** Grimbeorn, Beorning Chieftain 14_6;
Dasron, Merchant From Dorwinion 13_29; Erland, Advisor to Brand 2_21. Grimbeorn (the only Grimbeorn; strength 4,
vitality 5) is healed at the start of each turn, and his maneuver offer needs a minion in his owner's hand. Dasron
(companion, strength 6, vitality 3) makes a fellowship offer only with 2 Free Peoples culture tokens to remove.
Erland (ally, home 3F, strength 5, vitality 2; cheat him in, he needs Gandalf spotted) stops Shadow players looking
at the hand.

**A [gondor] ranger who is not unique; a [gondor] Man who is no ranger.** Ranger of Osgiliath 7_116; Gondorian
Knight 5_35. Both Men, cost 2, strength 6, vitality 3, not unique. The Ranger has a regroup ability (discard 2
cards from hand to exhaust a minion) used only on purpose, and beats an Uruk Savage without killing it; Knight is
the Knight's whole text. Knight of Gondor 5_36 is the same Man at strength 5 (Knight; a fellowship ability that
discards a [gondor] condition to heal him): the pair for the two sides of "strength less than 6" with no strength
given by hand (Card_06_076_Tests).

**A [gondor] artifact that someone bears.** Ring of Barahir 6_55; Narsil, Blade of the Faithful 9_34. Ring of
Barahir goes on any Aragorn (17_93 makes its bearer a [rohan] companion); an optional start-of-turn trigger, so
attach it after `StartGame()`. Narsil 9_34 goes on a [gondor] Man, the Merchant included; Narsil 17_31 on Elendil
or Isildur only. The two Narsils share a unique title: never both in play. The Shards of Narsil 3_44 is a [gondor]
artifact nobody bears. The nearest miss for "spot The Shards of Narsil", a [gondor] artifact of another title in the
support area: Seeing Stone of Minas Anor 9_37 (a regroup ability only), or Gilraen's Memorial 101_22 (a fellowship
ability only; Card_03_046_Tests). Both go in by `MoveCardsToSupportArea`, which skips their costs to play.

**Two hand weapons on one bearer by their own text; a weapon that bars every other.** Flaming Brand 2_32; Knife of
the Galadhrim 9_17; The Balrog's Sword 2_50; Andúril, Flame of the West 7_79. Flaming Brand ([gondor] possession,
cost 0, strength +1, on a Man; strength +2 and damage +1 only against a Nazgûl) and The Balrog's Sword ([moria]
artifact, strength +3, damage +1, on The Balrog) each say "This weapon may be borne in addition to 1 other hand
weapon"; Knife of the Galadhrim (unique [elven] possession, cost 1, strength +1, on a [gondor] Man; a skirmish
ability that discards it) says so only "If bearer is Aragorn". Andúril (unique [gondor] artifact on Aragorn,
strength +2, vitality +1) is the other way: "Discard other weapons he bears. He is damage +1 and cannot bear other
weapons", so it never stands beside a bow or a second weapon of any class (a board with both is no board). Narsil
9_34 is the artifact hand weapon without the bar.

**A card a [rohan] Man may bear that is no possession (beside "a possession" played on him).** Brought Down from
Inside 19_24; Protecting the Hall 11_149; Herugrim (the two PC errata) 54_280, 57_236. The first two are [rohan]
conditions, "Bearer must be a [rohan] Man": 19_24 offers a transfer at the start of each skirmish while another
[rohan] Man is in play and goes back to hand at the end of a turn its bearer won no skirmish in; 11_149 has toil 2
and "Assignment: Discard this condition from play to make bearer defender +1". The Herugrims are [rohan] artifacts,
hand weapons, "Bearer must be Théoden" (so the Man is a Théoden: Son of Thengel 4_292, cost 2, vitality 2, or King of the Golden Hall 5_93), one
unique title between them; no other artifact for a [rohan] Man was found.

**A Shadow card that is not what its culture mostly is.** Morgul Creeper 8_75; Strength Born of Fear 1_278. Morgul
Creeper is a [wraith] Orc (strength 8, vitality 2; archery +2 only at 3 burdens): the culture without the race for
"a Nazgûl". Strength Born of Fear is a [sauron] support-area condition whose skirmish ability costs (3): a [sauron]
card that is no minion, for "a [sauron] minion". A [moria] minion that is no Orc: Vile Tentacle 101_36 ("A tentacle
that stays put").

**An [orc] minion who is no Orc, and other [orc] cards.** Half-troll of Far Harad 14_12. [orc] Half-troll, cost 3,
strength 8, vitality 2, site 4; its only text is an optional start-of-assignment trigger that needs (2) and more
companions than minions. Tells `culture(orc)` from `orc`. A cheap [orc] Orc: Scurrying Goblin 11_137 (cost 1,
strength 5, vitality 1, site 4; adds (2) only when played at an underground site beside another [orc] minion). An
[orc] card that is no minion: The Beckoning Shadow 12_83 (support-area condition; to play, exert an [orc] minion,
so cheat it in; nothing below 5 burdens and an empty Free Peoples draw deck).

**A Nazgûl who is not [ringwraith].** Ulaire Attea, Khamul of the East 103_61. [raider], strength 12, vitality 3;
his text needs 3 burdens. Fierce, so keep him out of tests that run past the skirmishes. He may bear a Nazgûl's
mount: with Fell Beast 6_83 (strength +2; to play, discard 2 cards from hand: attach it) he is the one mounted
[raider] minion who is no Southron, and at 14 he overwhelms a companion of 7 or less.

**An [uruk-hai] minion who is no Uruk-hai; an Uruk-hai of another culture.** Saruman, Master of the White Hand
17_116; Cirith Ungol Guard 10_81. Saruman: [uruk-hai] Wizard, unique, cost 4, strength 8, vitality 4, site 4; each
companion is resistance -1, and a skirmish ability (exert him to discard a condition borne by a companion
skirmishing an [uruk-hai] minion) used only on purpose. The printed culture without the race for "an Uruk-hai": no
ad hoc culture is needed. Cirith Ungol Guard (cost 3, strength 9, vitality 2, site 6) and Cirith Ungol Sentry 10_84
(cost 2, strength 8, vitality 2) are [sauron] Uruk-hai whose whole text is Damage +1.

**A Southron who is no Man; a mounted minion that is no Man.** Bladetusk Rearguard 103_43; Bladetusk Charger
103_41. Rearguard: [raider] artifact, cost 4, Support Area; "Maneuver: Remove (5) to make this artifact a fierce
mounted Southron minion until the end of the turn that is strength +3 and ambush (1) for each Southron stacked on
it": with nothing stacked it is strength 5, vitality 5, fierce and mounted. Charger: [raider] possession, cost 3,
Support Area; it becomes one (strength 4, vitality 4, +4 and ambush (1) for each Southron stacked) by stacking 1 or
more Southron Men from play as its cost, and adds a threat at the start of each skirmish it is in. Bladetusk
Matriarch 103_42 is the third ("A Shadow card of twilight 5 in play that is no minion"). They are the printed
Southrons who are no Men, so "an Orc made a Southron" is still a keyword given to a card that no printed card
resembles.

**A Shadow card of twilight 5 in play that is no minion.** Bladetusk Matriarch 103_42. [raider] artifact, unique,
cost 5, Support Area keyword. Its optional trigger answers a Southron being played; its Maneuver ability removes
(6) to make it a minion until the end of the turn, so keep the pool under 6 in a maneuver phase. Plotting 7_67
discards itself at the start of the regroup phase and Endless Night 103_96 cannot be discarded: neither serves in a
regroup discard choice.

**A Shadow card a companion bears; a Shadow possession in play with no minion on the table.** Spider Poison 8_28.
[gollum] possession with the Support Area keyword: it lies legally in the Shadow player's support area (the Shadow
possession beside "Free Peoples possession", 92_26). Its Skirmish action (remove a threat or a burden) transfers it
to a companion skirmishing Shelob, so cheat it onto a companion; there its bearer must exert each time he is
assigned to a skirmish, and nothing else.

**A Shadow minion bearing a weapon and an item that is none; a plain weapon for a [sauron] Orc.** The Witch-king,
Lord of Angmar 1_237 with The Pale Blade 1_221 and Black Steed 1_208; Banner of the Eye 6_98. Pale Blade: unique
[wraith] hand weapon, cost 2, strength +3, "Bearer must be The Witch-king. He is damage +1"; its Response needs him
to win a skirmish and a Free Peoples condition to discard. Black Steed: [wraith] mount, cost 1, strength +2 (and +2
more at a plains site: site 2 of `FellowshipSites` is plains), on a Nazgûl; discarded at an underground site. The
Witch-king (fierce, 14, +2 for each other Nazgûl) then bears a weapon and a possession that is no weapon. Banner of
the Eye: [sauron] hand weapon, cost 0, strength +1, "Bearer must be a [sauron] Orc"; its optional trigger on a
skirmish won adds (1) for each site its owner controls. The [men] minions' weapons are under "A Hobbit who is not
[shire]" (Ted Sandyman).

**A [wraith] condition on a minion; on a companion or an ally; a character who cannot be healed.** In Twilight
19_34; Black Breath 1_207; Wounded 4_215. In Twilight: bearer must be a [wraith] minion, strength +2; attach it
(its play offers a draw) and keep it off a minion that wins a skirmish (the win offers a transfer). Black Breath:
support area; "Skirmish: Transfer this condition ... to a character skirmishing a Nazgûl" (an ally too); no limit
per bearer in its text, so two on one character is a board in play; wounds and burdens may not be removed from its
bearer (burdens only while the Ring-bearer bears it). Its bearer is the character who cannot be healed, for the
board where "heal X or <do Y>" has a branch that cannot be done in full: the heal is not offered and the other
branch is carried out with no question (F-0310; Card_10_030_Tests). Wounded ([isengard] condition on a Free Peoples
Man, "Wounds may not be removed from bearer"; its play exerts an [isengard] Man and the bearer: attach it) is the
other such card, read and not run.

**A Shadow condition played on a companion with nothing to spot or exert; one that goes on a Free Peoples Man
only.** Weary 4_212; Banished 4_139. Weary: [isengard], cost 1, search, "Plays on a companion or ally. Limit 1 per
character"; its trigger needs an [isengard] tracker. Banished: [isengard], cost 0, "To play, exert Saruman or an
[isengard] Man. Plays on a Free Peoples Man" (Gríma 4_154, vitality 3, to exert); its trigger answers another
character's lost skirmish. With one Man among the companions Banished shows a bar on him by not being offered at
all.

**A [wraith] item that is no possession; chosen Shadow cards put in the discard pile by a legal play.** Ring of
Asperity 9_42; Fell Beast 6_83. Ring: unique [wraith] artifact, cost 0, strength +1, on a Nazgûl; when played it
may wound an unwounded companion, and it answers a reconcile. Fell Beast: [wraith] mount, cost 0, on a Nazgûl
(bearer is fierce), "To play, discard 2 cards from hand": with two other cards in hand those two are discarded.

**A pipeweed that is not [shire]; pipes of two cultures.** Ship of Smoke 18_27; Bilbo's Pipe 1_285, Aragorn's Pipe
1_91. Ship of Smoke: unique [gandalf] support-area condition, cost 0, nothing needed to play it; its one text is an
optional trigger on a pipeweed being played, which needs Gandalf spotted and the Ship in play. Without it "a
pipeweed" narrowed to [shire] passes every test (92_4). The pipes are unique possessions of cost 1, on a Hobbit and
on a [gondor] companion; Gandalf's Pipe 1_74 (cost 0) goes on Gandalf alone, so with no Gandalf it is the pipe
nobody can play. Old Toby 1_305 ([shire] pipeweed possession, cost 1) offers a draw when played: an offer to
answer. Halfling Leaf 17_106 is a [shire] pipeweed CONDITION (to play, spot a Hobbit). The pipeweed that cannot be
played, from the discard pile or anywhere, is Wizard's Stash 103_7 ([gandalf] possession, unique, cost 2, "Bearer
must be a Wizard") once no Wizard is in play; every other pipeweed finds a bearer or the support area.

**Tales of each kind, for "each tale".** Thorin's Harp 18_3; The Saga of Elendil 1_114; Scroll of Isildur 9_36;
Banner of the White Tree 3_39. Harp: [dwarven] support-area condition, not unique, cost 0. Saga: [gondor] condition
borne by a [gondor] companion. Scroll: unique [gondor] artifact, cost 1, to play spot a [gondor] Man; the
Ring-bearer is resistance +1 for each [gondor] artifact. Banner: [gondor] Maneuver event, cost 0, never spotted.
Every tale is on a card of a Free Peoples culture.

**[shire] tales of each kind, for "a tale" or "a [shire] tale".** Stone Trolls 1_314; There and Back Again 1_317;
Three Monstrous Trolls 3_114; Red Book of Westmarch 2_113. Stone Trolls: condition any character bears, cost 2, not
unique, and so the Free Peoples card a MINION can bear, for a text that reaches cards on "the companion he was
skirmishing" (4_32); to play, spot Bilbo, so it is cheated on, or put on a minion legally by Bilbo, Bearer of
Things Burgled 9_49 ("Regroup: Add a burden to play a [shire] tale"; laid after the move, which his required move
trigger answers: Card_01_314_Tests); strength +3 only against a Troll or Uruk-hai. There
and Back Again: unique condition on a Hobbit companion, cost 1; "Maneuver: Discard this condition to make each
Hobbit companion strength +2", used only on purpose. Three Monstrous Trolls: support-area condition, cost 0; its
Response needs a Hobbit ally to exert. Red Book: unique POSSESSION in the support area, cost 2; its trigger needs
Bilbo. The tales of other cultures are under "Tales of each kind".

**A fortification that is no condition (beside "a fortification": every other is a condition).** Stronghold of My
Fathers 102_48. [rohan] POSSESSION, unique, Fortification, Support Area, cost 1; to play, spot 2 valiant companions
(two Household Guards 5_83) or 3 [rohan] companions; a Response to a wound on a [rohan] companion only. Laid in a draw
deck beside First Level 7_92 or Arrow-slits 5_80, it tells `fortification` from `condition,fortification` (10_117, 103_23).

**A [rohan] possession with no bearer (support area), and beacons to hinder for it.** Northern Signal-fire, Flame of
Calenhad 103_86 ([rohan] possession, 2 dots, Beacon, Support Area, cost 2; to play, hinder 2 beacons; a maneuver
ability only). War-beacon 103_35 and Northern Signal-fire, Flame of Amon Din 103_28 ([gondor] beacons, cheated into
the support area) are the two to hinder: their abilities are fellowship ones and a response to a wound on an
unbound Man. With one beacon in play the Flame cannot be played, with both it can (Card_07_321_Tests, beside
`PlayableOn(any)` in a play-from-discard requirement). Two Signal-fires in play is the title's 2 dots, no more.

## Wounds, exertions, heals, kills and discards that a printed card does

**Wounds in the regroup phase.** Morgul Slayer 3_93. Strength 7, vitality 2; "Regroup: Exert this minion to wound a
companion (except the Ring-bearer)." Two copies wound one companion twice in one regroup phase, which is how to
read "until the regroup phase" at its boundary. One wound from it is also the `-` of a trigger on an exertion
whose effect would last the current phase: a skirmish wound shows nothing there (Card_13_075_Tests).

**A Free Peoples wound on a minion in the maneuver phase.** Defend It and Hope 4_115. [gondor] Maneuver event, cost
0: exert a [gondor] Man to wound a minion once, or twice if it is an Uruk-hai. Kills a minion of 1 vitality to
leave a board with no minion to spot. Or Baruk Khazâd 5_5: [dwarven] Maneuver event, cost 0, exert a Dwarf to
wound a minion; the Free Peoples player then takes the next maneuver action, not the Shadow player (Card_05_005_Tests).

**The only minion of a skirmish removed during it, where the companion is neither the Ring-bearer nor a
signet-bearer.** Long I Fell 4_97. [gandalf] Skirmish event (Spell), cost 2: "Spot Gandalf to prevent all wounds to
him. Any Shadow player may make you wound a minion to prevent this." Played in any companion's skirmish with
Gandalf in play: `FreepsPlayCard(fell)`, `ShadowChooseYes()`, `FreepsChooseCard(minion)`. A minion one wound from
death is killed, the skirmish ends at once with the companion its winner (LR 2: the minion is the losing
character), and a win trigger of his raises the "Required responses" ordering (`Card_15_038_Tests`). It is also
a wound to a minion in a skirmish it is not in: the Free Peoples player chooses any minion, the bearer of Pavise
11_94 among them while another skirmish is fought (`Card_11_094_Tests`).

**A minion killed, and a minion discarded and not killed, by the Free Peoples player in the maneuver phase (the two
sides of "each time a minion is discarded", "is killed").** Defend It and Hope 4_115; What Are They? 1_119 (with
Aragorn, Ranger of the North 1_89 and Man of Bree 11_90). Defend It and Hope: [gondor] Maneuver event, cost 0,
"Exert a [gondor] Man to wound a minion once (or twice if that minion is an Uruk-hai)": kills a minion of 1
vitality. What Are They?: [gondor] Maneuver event, cost 1, "Spot a ranger to discard a roaming minion". Man of
Bree: [men] Man, cost 2, vitality 1, site 4 (roaming at site 2), strength 4 and +1 for each card in the Free
Peoples hand. With one minion on the table neither event asks anything (mechanics/wounds/KilledIsNotDiscardedTests,
Card_17_066_Tests). A COMPANION killed and one discarded in the regroup phase: Morgul Slayer 3_93 and Fierce in
Despair 7_148, in the same class.

**A companion discarded from play, not killed.** Fierce in Despair 7_148. [raider] Shadow Regroup event, cost 7:
"Spot 2 [raider] Men to discard a companion (except the Ring-bearer)"; the Shadow player chooses. Two Easterling
Axemen 4_224 (not unique; their own text needs 2 burdens) are the Men. He goes to the discard pile with what he
bears and the dead pile stays empty: the nearest miss of "killed" (Card_94_004_Tests).

**A companion taken out of play in the fellowship phase.** Saved From the Fire 8_20. Free Peoples Fellowship event,
cost 3: spot Gandalf (by name) and place a companion, not the Ring-bearer, in the dead pile (a kill, as its cost)
to take 0 to 3 cards of his culture from the draw deck, which is shown first: see "Searching a deck shows the deck
first" in the pitfalls.

**Something that answers a wound TAKEN (to show one was not).** Rank and File 10_96; Lost to the Goblins 1_189.
Rank and File ([sauron] condition, support area): an optional trigger each time a roaming [sauron] minion takes a
wound, in any phase (adds a [sauron] token to itself); its two abilities are used only on purpose. Lost to the
Goblins ([moria] Shadow Response event, cost 0, held in hand): offered when the Ring-bearer exerts or takes a
wound, and not when the wound is replaced or prevented.

**A wound to a chosen companion who is in no skirmish, during another's skirmish.** Orkish Skirmisher 18_90. [orc]
Orc, not unique, cost 4, strength 9, vitality 3, site 4; "Each time this minion wins a skirmish, you may exert him
twice to wound a companion." Any companion may be chosen, and the trigger is carried out before that skirmish phase
ends. Two copies on one companion (ad hoc defender +1, and strength enough not to be overwhelmed) give two such
wounds in one skirmish (Card_13_020_ErrataTests).

**A wound on the Ring-bearer, phase by phase (a Ring's "about to take a wound").** Little Snuffler 13_52; Gate
Picket 6_101; Goblin Marksman 1_176; Orc Soldier 1_271; Gollum, Dark as Darkness 9_28; Isengard Worker 3_62.
Shadow: Little Snuffler ([gollum] event, cost 2; in region 1, spot Gollum to wound a companion of the Shadow
player's choice). Maneuver: Gate Picket (exerts twice; the Free Peoples player's choice of characters to spare is
declined with `FreepsDeclineChoosing()`, and every companion and ally is wounded). Archery: the Marksman. Skirmish:
Orc Soldier ("Exert this minion to wound a character he is skirmishing"). Regroup: Gollum ("Exert Gollum twice to
wound the Ring-bearer", once; with a burden on the table name the action, `ShadowUseCardAction(gollum, "Wound the
Ring-bearer")`; his Shadow ability only EXERTS the Ring-bearer, the near miss) and Isengard Worker (the Free
Peoples player chooses the companion). Card_01_001_Tests, Card_01_002_Tests.

**A wound in an assignment phase.** Southron Fanatic 10_49, on a Man (Isildur, Bearer of Heirlooms 9_33 as
Ring-bearer). Fanatic: [raider] Man, not unique, cost 3, strength 8, vitality 2, site 4; "Each time a Man is
assigned to skirmish this minion, wound that Man" (required; the only printed text found that wounds on an
assignment). Fierce by hand, it wounds again in the fierce assignment phase, the one phase between a skirmish and
the regroup phase that is no skirmish. Isildur (strength 7, vitality 3, resistance 6) as the scenario's Ring-bearer
adds 2 burdens as each skirmish of his starts when nobody else can exert: stop before his second
(Card_01_002_Tests).

**A Shadow special ability in the archery phase.** Wreathed in Shadow 1_238. [wraith] support-area condition, cost
0; "Archery: Remove (1) to make the fellowship archery total -1". To play, spot a Nazgûl: cheat it in and set the
pool to 1. The Shadow doer for "the Free Peoples player uses an archery special ability"; Tower Assassin 2_93
(maneuver: exert to wound an ally) is the one for maneuver.

**A Shadow card that exerts a companion.** Mûmak Commander, Giant Among the Swertings 15_86. [men] Man, unique,
cost 6, strength 14, vitality 3, site 4; "Maneuver: Exert Mûmak Commander twice to exert a companion twice (except
the Ring-bearer)." Two exertions of the minion and then two of one companion, by one effect.

**Every character exerting at once, by one effect (an "is about to exert" response that picks the exertion it
spares).** Devilry of Orthanc 5_49. [isengard] Machine condition, unique, support area; "Maneuver: Spot 8
[isengard] tokens here to exert every character" (cheat the 8 tokens on and read them; it then discards itself).
Companions, allies and minions exert together, a Shadow card's effect and no cost: an ally and a minion are the
invalid candidates of the choice. An exhausted companion does not exert, so exhaust the Ring-bearer to leave one
companion of another culture and race as the only exertion that fires the trigger (Card_01_085_Tests).

**A Free Peoples card that exerts a companion of the player's choice: an event, an effect and not a cost; and a
card with no side that does.** Dear Friends 2_98; Isildur, Sword-bearer 18_54; Ettenmoors 1_331. Dear Friends:
[shire] Fellowship event, cost 0, "Exert a companion to place a Free Peoples card from your discard pile beneath
your draw deck": a cost paid for a card that is no character; offered with an empty discard pile, and then places
nothing. Isildur ([gondor] Man, unique, strength 7, vitality 3, resistance 1): a required "Each time Isildur loses
a skirmish, exert a companion", an exertion that is an EFFECT (`FreepsResolveRuleFirst()` first: pitfalls, "When a
wound kills"); when he wins he heals a companion. Ettenmoors, site 2 of `FellowshipSites`: "Skirmish: Exert your
companion or minion to make that character strength +2" takes any companion of the user's, skirmishing or not, and
a site has no side: the invalid doer for "by a Free Peoples card" (Card_94_020_Tests).

**One cost that exerts a character more than once (the R of "each time ... exerts").** Tom Bombadil, The Master
9_52. [shire] ally with no race, vitality 9 (to play, remove 2 burdens or 2 threats: cheat him in); "Skirmish: Spot
2 [shire] companions and exert Tom Bombadil X times, where X is the fellowship's site number, to cancel a skirmish
involving a [shire] companion". At site 2, with Frodo and Sam and Sam in a skirmish, he exerts twice for one cost
and the skirmish is cancelled (`Card_02_040_Tests`).

**A heal of a minion: by the Free Peoples player; by the Shadow player.** Hrethel, Rider of Rohan 6_95 ("Skirmish:
Heal a minion skirmishing Hrethel to make Hrethel strength +3 (limit +3)"; to play, spot a [rohan] Man); Uruk
Shaman 1_152 ("Maneuver: Remove (2) to heal an Uruk-hai"; damage +1). Hrethel is the only printed card by which a
Free Peoples player heals a minion: the minion candidate for a text that counts or limits "your" heals of "a
character" (92_35). His heal is the COST of his ability (`heal` of `choose(minion,inSkirmishAgainst(self))`):
lay the minion wounded and skirmishing him, as `Card_06_095_Tests` does. A Wizard minion who heals, by his own text: Saruman, Agent of the Dark Lord 12_144 (unique
[uruk-hai] Wizard, strength 8, vitality 4, lurker; "Each time a companion is killed, you may heal Saruman"), laid
wounded, when Gandalf, Mithrandir 6_30 ("Each time the fellowship moves, wound Gandalf") with 3 wounds dies at the
move (`Card_13_080_Tests.ASarumanWhoHealsMakesItAMinion`).

**A heal of a companion by each kind of source: a site, one event three times, the Shadow player, a delayed
trigger.** Balin's Tomb 1_343; Peace of Mind 7_46; Dark Temptation 12_163; Enemy in Your Midst 18_30; End of the
Game 10_30. For "each time the Free Peoples player heals a companion" (`Card_17_140_Tests`). By a site's text, and
so by no card of his side: Balin's Tomb, site 4 of `FellowshipSites` ("Maneuver: Discard your tale from play or from
hand to heal your companion"); its cost is a choice "from play / from hand" whenever a tale is in hand as well, so
put every other tale of the deck in the discard pile at the start (F-0264): The Saga of Elendil dealt in by a
turn-skipper failed the test about one run in five, and so
does Gondor Bowmen 3_41, a [gondor] tale too (Card_10_020_Tests). With no tale in hand the tale in play is
discarded unasked; and on the "from hand" branch a lone tale in hand is discarded unasked as well, so lay two to read that choice (Card_01_343_Tests, dec-b1_336). One event that heals one companion three times: Peace of Mind
([gandalf] Regroup event, cost 4, "Spot Gandalf to heal an unbound Hobbit 3 times"); three heals, each answered in
its turn. The SHADOW player healing a companion: Dark Temptation ([wraith] support-area condition, "Each time you
play a Nazgûl, you may heal the Ring-bearer twice to add a burden": the heals are its cost) and Enemy in Your Midst
([gollum] Shadow event, cost 0, "Heal a Ring-bound companion to reinforce a [gollum] token"; read in its definition,
laid by no test yet). A heal an event leaves behind: End of the Game ([gondor] Skirmish event, cost 0: an exhausted
[gondor] companion is strength +2 and, if he wins, heals or is damage +1), whose required trigger is ordered against
"Resolve skirmish damage" (`FreepsResolveActionOrder("Required trigger from")`).

**A heal of "a companion" with a before and an after.** Master of Healing 3_22, twice, beside Elrond, Lord of
Rivendell 1_40. [elven] Fellowship or Regroup event, cost 1: "Spot Elrond (or 2 [elven] allies) to heal a companion
or ally". Only the wounded are offered. Elrond is an ally (his start-of-turn heal reaches allies of site 3 only:
wound the companions after `StartGame()`). It is also the heal of a Wizard by legal play, in the fellowship phase
or the regroup phase: Gandalf 1_364, or the ally Radagast 31_17 (`Card_13_080_Tests`).

**A heal that names several companions at once (one "is about to heal" window for all of them).** Minas Tirith
Third Circle 7_352, King block site 6, put as `site6` on `FellowshipSites`: "If the fellowship moves from this site
during the regroup phase, heal each Hobbit companion" (`heal` of `all(hobbit,companion)`, one effect). `SkipToSite(5)`,
wound the Hobbits, move to it, and move on from it in the regroup phase: an "about to heal" response sees them
together (Extraordinary Resilience 1_287 replaces Frodo's heal and Sam's goes on: `Card_01_287_Tests`).

**A way to discard a [moria] condition.** Song of Durin 3_5. Free Peoples event: "discard up to 2 [moria]
conditions".

**Other ways to discard Shadow cards.** Sleep, Caradhras 1_84. Fellowship event: exert Gandalf to discard every
condition. One [sauron] minion, condition or possession of the Free Peoples player's choosing: Stand Against
Darkness 1_63 (maneuver; exert an Elf). Every Shadow card a minion bears, of whatever types, at once: Fall Back
5_34, with City Wall 5_33 cheated onto the minion. The Shadow player discarding his own [sauron] condition as a
cost: Orc Veteran 3_100.

**A discard the Free Peoples player makes by no card of his own; a Free Peoples card that has the Shadow player
discard.** Heights of Isengard 11_244; Introspection 12_29. Heights of Isengard (Shadows-block site, battleground):
each time a [gandalf] companion wins a skirmish the Free Peoples player may discard a condition from play; the
source is a site, which has no side (path and play in Card_18_029_ErrataTests). Introspection ([gandalf] Fellowship
event, cost 2, spot a [gandalf] Wizard): away from a battleground its only branch has the SHADOW player choose and
discard one of his own conditions. Brooding on Tomorrow 10_14 does the same on a loss of initiative.

**A doer that is no Shadow card, and a Shadow item, for "Shadow cards may not discard cards from your hand or from the
top of your draw deck".** Midgewater Moors 1_333; Uruk-hai Sword 1_160. The Moors (FOTR site 2, plains): "Each time a
minion is played, the Free Peoples player discards a card from hand", written `forced: true`, so the bar is asked of
it and only its side lets it through; it is the site doer for "a Shadow card makes you discard from hand" too (Old
Noakes 3_111, Card_03_111_Tests). The bar is asked only of a discard written `forced: true`: Search Far and Wide
4_56's cost (the top card of the deck) writes `false` and shows nothing as a candidate, whatever its side. The Sword
([isengard] hand weapon on an Uruk-hai, Uruk-hai Band 4_202): "Each time bearer wins a skirmish, the Free Peoples
player must discard the top card of his or her draw deck"; the loser's wound and its trigger are ordered by the Free
Peoples player (Card_04_301_Tests).

**A discard the SHADOW player makes: by a keyword across card types, and by a site's text; a Shadow card that has
the Free Peoples player discard his own cards; a Shadow card's discard that is not from play.** Stricken Dumb 2_80;
Buckland Homestead 11_230; Why Shouldn't I Keep It? 3_105; Massing in the East 3_92. Stricken Dumb ([wraith]
Maneuver event, cost 2): "Exert The Witch-king to discard a tale or weapon", one choice over every card type and
culture. Buckland Homestead (Shadows-block site): "Shadow: Play a Nazgûl to discard a condition from play", the
mirror of Heights of Isengard; on a Fellowship path it is laid with `remove("site9")`, `put("homestead", "11_230")`
and `MoveCardToAdventurePath` before the first move (Card_01_306_Tests). Why Shouldn't I Keep It? ([sauron]
Maneuver event, cost 0, spot a [sauron] minion): discards Bilbo unless the FREE PEOPLES player discards 2 of his
own conditions (Q-0171), the mirror of Introspection. Massing in the East ([sauron] Regroup event, cost 1, exert a
[sauron] minion): discards from the top of the Free Peoples draw deck, a card for each of Dwarf, Elf, Man and
Wizard among the companions.

**A card that answers a possession discarded from play.** Sword Rack 11_158; Countless Companies 12_58. Sword Rack:
[rohan] support-area condition, "Response: If a [rohan] possession is discarded from play and no more than one
possession is stacked here, stack it here" (Household Guard 5_83 bearing Rider's Spear 4_288, or Rohirrim Shield
5_91, whose regroup ability discards itself). Countless Companies: the same for a [men] possession, on the Shadow
side (Corps of Harad 11_73 bearing Henchman's Dagger 18_69). Offered once for each possession discarded, one that
went with its bearer included, and not again once it is stacked (F-0357); decline it to leave the possession in the
discard pile.

## Burdens, threats, culture tokens, hinder and The One Ring

**A burden removed as a Shadow card's COST; as a Free Peoples card's EFFECT.** Ring of Rancor 9_44; Frodo's Cloak
4_303. Rancor: unique [wraith] artifact (ring), cost 0, vitality +1, on a Nazgûl; "Maneuver: Remove a burden to
heal each Nazgûl" (wound the Nazgûl first); its Response answers a reconcile by returning the bearer to hand.
Cloak: unique [shire] possession, cost 2, on Frodo; away from sites 1T to 4T, "Skirmish: discard this possession to
remove a burden and heal Frodo" (at those sites it adds a burden to cancel his skirmish).
A burden removed by a non-Hobbit CHARACTER's own text (the F of "except by a Hobbit's game text"): Járnsmid, Barding
Emissary 12_30, unique [gandalf] Man companion; "At the start of each skirmish involving Járnsmid, you may remove a
burden (or 2 burdens if you can spot another [gandalf] companion)": two at once beside Gandalf 1_72 (Card_03_113_Tests).

**A burden a Shadow card adds: one the Free Peoples player chooses; two at once; one a minion's own ability adds. A
burden a site adds.** Desperate Defense of the Ring 1_244; Eye of Barad-dûr 5_96; Orc Miscreant 11_131; Eregion Hills 3_116. Defense 1_244: [sauron] condition on a companion (cheat it on; to play it exerts a [sauron]
Orc): each time bearer is assigned to a skirmish the Free Peoples player chooses "Discard 3 cards from hand" or
"Add a burden"; give him 3 or more cards in hand, or there is no choice to put (F-0310). Eye: [sauron] event, cost
0, whose Response is played when a Free Peoples player reveals it from the Shadow hand, and adds 2 burdens:
Glamdring 1_75 on Gandalf 1_72 reveals that hand in the fellowship phase (`FreepsUseCardAction(glamdring)`,
`FreepsDismissRevealedCards()`, `ShadowPlayCard(eye)`; Card_04_316_Tests, Card_05_096_Tests). Miscreant: [orc] Orc
minion, vitality 3, "Regroup: Exert this minion to add a burden" (cheat it in; to play it spots an [orc] minion),
nothing else in the way, and usable twice in one regroup phase. Eregion Hills, of no side: site 4 of a custom FOTR site
map, "When the fellowship moves to Eregion Hills, add a burden" (Card_03_110_Tests, beside Sméagol 7_71's "To play,
add a burden" for a Free Peoples doer).

**A threat removed as a Shadow card's COST; as a Free Peoples card's EFFECT (a possession's, a condition's); as a
Free Peoples card's COST; by a site, which has no side.** Sauron's Hatred 7_310; Sting, Elven Long Knife 18_113;
Hornblower Leaf 17_108; Tom Bombadil, The Master 9_52; City Gates 7_344. Hatred: [sauron] support-area condition,
cost 0 (to play, spot a [sauron] Orc: cheat it in); "Shadow: Remove a threat to play a [sauron] Orc without paying
any roaming penalty" (Orc Chieftain 1_266 in hand). Sting: unique [shire] hand weapon, cost 2, strength, vitality
and resistance +1, on Bilbo or Frodo; "Regroup: Exert bearer to remove a threat". Tom: "To play, remove 2 burdens
or 2 threats", the only Free Peoples card of the Decipher sets that pays in threats (Ethereal Banner 103_21 is the
PC one): with no burden laid the threats are the only way to pay. Leaf: [shire] support-area condition, pipeweed,
cost 1 (to play, spot an unbound Hobbit: cheat it in); "Regroup: Discard a pipeweed from play to remove a threat",
the Free Peoples doer that is neither a character nor an item (beside Tom and Sting alone, "Free Peoples cards"
narrowed to characters and possessions passes); it has a second regroup action, so `FreepsUseCardAction(leaf,
"Remove a threat")`. City Gates: site 5 of the King block (`putAll(KingSites); put("site5", "7_344")`), "When the
fellowship moves to this site, add 3 threats. When the fellowship moves from this site, remove 3 threats":
`SkipToSite(5)` with three companions arrives with the 3 lying, and they go as the fellowship moves on. Two threats
want two companions (Card_10_047_Tests).

**A place where no culture token can be added, removed or reinforced.** Redhorn Pass 13_194. Shadows-block site
(mountain, Shadow number 2): "Culture tokens cannot be added, removed, or reinforced." The `C-` board for a cost of
adding or removing a token: the action is not offered there (Scouring of the Shire 68_112). A no-cost "you may
place (reinforce) a [culture] token" is not offered there either, withheld by the engine (F-0423, F-0476: Secret
Folk 4_34, Arod 13_1). On a Fellowship path it stands in place of a site no test reaches (pitfall "A Shadows-block
site path"; any key will do).

**Culture tokens: a card that SPOTS one, a card that COUNTS them, and cards that carry them.** Ranger of the South
18_56; Desert Wind 13_86; Noble Leaders 7_112, Heirs of Gondor 13_69, Garrison of Osgiliath 6_52; Ships of Great
Draught 8_65. Ranger: [gondor] Man, strength 5, +2 while a [gondor] token can be spotted. Desert Wind: [men] minion
(to play, spot a [men] minion: cheat it in); the minion archery total is +2 at 5 culture tokens of any culture. The
three unique [gondor] support-area conditions get tokens only as they are played (cheated in they have none:
`AddTokensToCard`) and act only in a skirmish. Ships: [raider] support-area possession, not unique, a Shadow and a
Regroup ability used only on purpose (mechanics/hinder/TokensOnAHinderedCardTests).

**A culture token placed and removed by legal play; a reinforcement of one token, and of several; a cost of 2
tokens.** A Path Appointed 8_42; Arod, Rohirrim Steed 13_1 with Suspended Palaces 6_10 and Stout and Strong 4_57;
Guarded City 13_68; Arod's Response. Path: [gondor] support-area condition, "Fellowship: Exert a [gondor] Man and
either a [dwarven] companion or a [rohan] companion to add a [gondor] token here. Regroup: Remove a [gondor] token
here to discard a minion". Arod (on a Dwarf): "When you play Arod, you may reinforce a [dwarven] token", the rules'
own example; its Response removes 2 [dwarven] tokens, from two cards if need be. Palaces takes 2 tokens as it is
played beside a Dwarf and discards itself as a cost in the fellowship phase; Stout and Strong takes one for each
skirmish a Dwarf wins. Guarded City: [gondor] event, cost 1, a [gondor] token reinforced for each possession the
spotted companion bears, each chosen for in turn (mechanics/inplay/CultureTokensTests).

**Any Ring put on in the maneuver phase by a printed card (a Ring's phase word read with the Ring ON).** Saruman's
Reach 1_137, with Uruk Savage 1_151. [isengard] Maneuver event, cost 0: "Exert an Uruk-hai to make the opponent
choose to either exert 2 companions or make the Ring-bearer put on The One Ring until the regroup phase." With the
Ring-bearer the only companion there are no two to exert: nothing is asked, the Ring goes on and no burden is
added. It stays on through the archery phase and the skirmishes (Card_04_001_Tests). pitfalls.md, "No cheat puts
The One Ring on", has the other two cards. The same event is the nearest miss of an "If the Ring-bearer puts on
The One Ring" trigger: with a second companion on the table, `FreepsChoose("Exert 2 companions")` and the Ring
stays off, so the trigger is not offered (Card_01_228_Tests, The Twilight World 1_228, lays both boards).

**A printed card that hinders the Ring-bearer (why a hindered Ring-bearer is a board in play).** For the Shire
103_108; One Ill Turn Deserves Another 103_38; Ill Wind 103_67. For the Shire (Free Peoples event) hinders "all
other Hobbits" in region 1; the two Shadow conditions hinder "a companion" and "a wounded companion". They show the
board is reachable; the test lays it with `scn.HinderCard(<the Ring-bearer>)`. Andúril 103_18 reaches unbound
companions only. Where hindering him is the ACT, Was He Dear to Thee? 103_104 ([sauron] Maneuver event, cost 3)
does it as a cost, whoever he is: "Exert a unique [sauron] or [ringwraith] minion and hinder the Ring-bearer to
make each minion strength +2 until the regroup phase" (Úlairë Lemenya 1_232 pays; nothing is asked).

**A printed card that hinders a character who is assigned or skirmishing (where that hindering is the act).**
Sméagol, Once Like Us 103_116; Úlairë Toldëa, Blessed with Brutality 103_79; Úlairë Attëa, Bestowed with Dominion
103_72; Shelob, Terror of Cirith Ungol 103_13 with Gollum 5_24. Sméagol ([shire] companion; his Assignment ability
needs a fellowship of [shire] companions only): exerts and hinders himself, adds (2), and 2 threats for a minion of
cost 5 or more, to hinder a minion. Toldëa (fierce, 13/3): an optional trigger each time he is assigned to a
STRONGER companion, exhausting himself to hinder that companion; it answers an assignment made by a card as well
(give the companion strength with `ApplyAdHocModifier`). Attëa (fierce, 13/3): "Skirmish: Hinder another minion",
used while he is assigned to nobody. Shelob (fierce): "Skirmish: If Gollum is skirmishing, exert Gollum and Shelob
to hinder a character he is skirmishing." The three minions are fierce, so once the skirmish is gone the game rests
on the fierce round's assignment actions, not on the regroup phase
(`mechanics/hinder/HinderedCharacterCannotSkirmishTests`).

**A card that restores a Free Peoples card by legal play (where the restoring is the act).** Mantle of the White
Wizard 103_3, with Gandalf 1_72, Gandalf's Staff 2_22 and A New Light 17_15. The Mantle in the support area: "Each
time you play a spell, you may hinder a [gandalf] artifact to restore a Free Peoples card." The Staff on Gandalf is
the artifact and makes the spell cost 0; A New Light is a Fellowship spell that does nothing with no minion in the
Shadow discard pile. Play it, accept the Mantle's trigger (the Staff is hindered with nothing asked) and choose the
card to restore: the Staff is a candidate beside it (Card_07_079_Tests). A reconcile is the other restore and needs
no card.

## Assigning, playing, drawing, revealing and stacking by a printed card

**A minion assigned by a card and not by a player's assigning.** Éowyn, Northwoman 17_96; Slow-kindled Courage
7_328; Shadowy Mount 12_171; The End Comes 1_169. Éowyn ([rohan] Man, strength 6, vitality 3): "Assignment: Exert Éowyn and assign a
minion to the Ring-bearer", the Free Peoples player's own action; the minion loses its keywords until the regroup
phase. Slow-kindled Courage ([shire] condition on Merry or Pippin: cheat it on) gives each minion "Assignment:
Assign this minion to bearer", which the SHADOW player uses after `FreepsPass()` (`ShadowUseCardAction(minion)`).
Shadowy Mount ([wraith] mount on a Nazgûl): the Shadow player makes the FREE PEOPLES player assign bearer to an
unbound companion bearing a [wraith] condition; Blade Tip 1_209 cheated onto the companion is that condition and
does nothing before its bearer's own next fellowship phase (on the other player's turn neither card is active),
where a required trigger wounds him: after the sanctuary-healing decision at a site 3 or 6, and in one "Required
responses" ordering with any other required action of that moment (Card_01_342_Tests). Shadowy Mount goes on a
Nazgûl only: for a minion of any other race the Shadow card that has the FREE PEOPLES player assign is The End
Comes ([moria] Assignment event, cost 0: "Spot 5 [moria] minions to make the Free Peoples player assign the
Ring-bearer to a skirmish"), played after `FreepsPass()`; he picks ANY minion for the Ring-bearer
(`FreepsChooseCard`), and Goblin Patrol Troop 1_177 with four Goblin Runners 1_178 are the five
(Card_12_098_Tests).

**A card played only to take the hand from four to three (a loss of initiative).** Stairs of Khazad-dûm 1_24
([dwarven] condition, cost 0, support area, no requirement) in the fellowship phase; Elendil's Valor 1_103 ([gondor]
Maneuver event, cost 0) in the maneuver phase, which picks a lone [gondor] companion unasked, so no `FreepsChooseCard`
follows it (Card_10_012_Tests, Card_10_108_Tests). Skirmish and regroup: pitfalls, "The loss of initiative comes with the play".

**A companion played, and an ally played beside it (for "each time a companion is played").** Gimli, Son of Glóin
1_13 ([dwarven] Dwarf, cost 2) and Boromir, Son of Denethor 1_97 ([gondor] Man, cost 3): no requirement to play and
no text that answers a play, so `FreepsPlayCard` in the fellowship phase asks nothing of its own. Bounder 1_286
([shire] Hobbit ally, cost 1, no requirement) is the ally played that must not set it off (Card_07_204_Tests).

**A companion played in the maneuver phase, after an event (the board for "make each ... until" read as a
snapshot).** Éomer, Éored Leader 12_112: "Maneuver: If you can spot more minions than companions, exert Éomer to
play a [rohan] companion from your draw deck." The deck is shown first: `FreepsDismissRevealedCards()`, then
`FreepsChooseCardBPFromSelection(card)` (pitfalls, "Searching a deck shows the deck first"). Théoden, Tall and
Proud 8_92 is a [rohan] companion with the Aragorn signet (Card_03_047_Tests).

**A minion's own assignment action, with no cost.** Reckless Pride 7_13; Ancient Blade 7_15; Unexpected Visitor
7_126. Free Peoples conditions on a [dwarven], an [elven], a [gondor] companion: 'Each minion gains this ability:
"Assignment: Assign this minion to bearer of <this card>."' `ShadowUseCardAction(minion, "<the card's name>")`
takes it; how a companion comes to have a minion before the Free Peoples player assigns. Offered only while minion
and bearer can both be assigned (F-0353, F-0354).

**A minion that comes into a skirmish already under way (the board for "each minion skirmishing ..." read as a
snapshot).** Controlled By The Ring 15_41 on Gollum, Vile Creature 7_59, the only printed card that puts a minion
into a skirmish in another's place. Condition, cost 1, "Bearer must be Gollum. Skirmish: If Gollum is not assigned
to a skirmish, discard this condition to have him replace a minion skirmishing a companion"; Gollum is strength 5,
vitality 4, and his own skirmish ability is used only on purpose. Leave him unassigned (`ShadowDeclineAssignments()`),
and once the effect is used `ShadowUseCardAction(<the condition>)` puts him where the minion was, the minion out of
the skirmish (Card_07_017_Tests.AMinionThatComesIntoHerSkirmishAfterTheUseIsNotMadeWeaker).
Where the text read is a keyword Gollum lacks ("lose fierce"), give it him by hand as a printed card can
(`ApplyAdHocModifier(new AddKeywordModifier(null, gollum, null, Keyword.FIERCE))`, as Emyn Muil 1_360 would):
Card_13_153_Tests.AMinionThatComesIntoTheBearersSkirmishAfterTheUseKeepsFierceAndOneItReplacedStaysWithoutIt.

**A Shadow card that is no event, played in a skirmish (the invalid candidate beside "a Shadow event is played").**
Army of Haradrim 5_70 with War Mûmak 5_78. Army: unique [raider] Man, Southron, cost 7, strength 14, vitality 4,
site 4; "Skirmish: Remove (3) to play a [raider] mount from your discard pile". Mûmak: [raider] mount on a
Southron, cost 3, strength +4, bearer is fierce; its own offer (add (5)) comes only when bearer overwhelms. With
the Mûmak in the discard pile and 6 in the pool `ShadowUseCardAction(army)` plays it on the Army, the only
Southron: a card played in that phase (`playedCardThisPhase` counts it: a mutation showed so, Card_10_116_Tests),
and the Army then has a fierce round to fight.

**A minion played outside a Shadow phase (the phase next door to "played each Shadow phase", "in the Shadow
phase").** Peril 6_105, or Spied From Above 6_84; then Úlairë Nertëa, Messenger of Dol Guldur 1_234. Peril
([sauron] support-area condition): "Regroup: Exert a [sauron] Orc to play a Nazgûl or [sauron] minion", cost -4,
exhausted after the play. Spied From Above ([wraith]): exert a Nazgûl twice, cost -8. Either plays Nertëa (cost 4)
from hand in the regroup phase, and with five companions (Frodo and four Dwarf Guards) her own "when you play"
offer plays ANY minion from the discard pile: `ShadowUseCardAction(peril)`, `ShadowAcceptOptionalTrigger()` with
one minion there (Card_01_350_Tests). In the ASSIGNMENT phase: A Shadow Fell Over Them 15_182 ([wraith]
support-area condition; "Assignment: Spot a Nazgûl and remove (4) to play a Nazgûl from your discard pile") plays
Nertëa (in the discard pile) beside Úlairë Lemenya 1_232, the pool set high, which is the legal play of "a minion
that comes into play afterwards" for a Maneuver event's snapshot (Card_10_016_Tests). In the maneuver phase itself,
read and not run: Stampeding Savage 17_53 ("Maneuver: Discard this minion and X minions stacked on a [men]
possession to play a [men] minion from your discard pile").

**A possession played on a companion outside the fellowship phase's own plays (by a card's ability, in a phase with
minions).** Éowyn, Daughter of Éomund 5_122 with Rider's Spear 4_288; Théoden, King of the Golden Hall 5_93. Éowyn:
[rohan] companion, strength 6, vitality 3; "Maneuver: Play a [rohan] possession on Éowyn to heal her": the play is
the ability's cost, `FreepsUseCardAction(eowyn)` with one such possession in hand plays it unasked, and a response
to the play is answered before her heal (Card_10_019_Tests). Spear: [rohan] hand weapon, cost 1, strength +2, on a
[rohan] Man. Théoden (read, not run): the same ability, healing a [rohan] ally.

**A Shadow card that cancels a Free Peoples event (tells an event's cost from its effect: what is asked before the
response is offered is cost, and what was paid stays paid).** The Mouth of Sauron, Bearer of Dread News 103_70;
Furious Hillman 14_10. Mouth: unique [wraith] Man, lurker, cost 3, strength 9, vitality 3, site 3; "Response: If a
Free Peoples event is played, exert this minion to cancel that event", taken with `ShadowActionAvailable(mouth)`
and `ShadowUseCardAction(mouth)`; the event goes to the discard pile, its twilight and costs paid. His "Other
minions cannot take more than 1 wound per phase" is in the way of a test that wounds minions (Card_10_016_Tests).
Hillman (read, not run): [men] Man, cost 3, strength 8, vitality 2; spots another [men] Man and discards itself to
cancel.

**A card moved out of ANOTHER player's discard pile (the other side of "your discard pile").** A New Light 17_15.
[gandalf] Fellowship event, Spell, cost 1, needs a [gandalf] Wizard spotted: the Free Peoples player chooses a
minion in the Shadow player's discard pile (`FreepsChooseCardBPFromSelection`) and it returns to its owner's hand;
he is then asked whether to reveal the Shadow hand ("you may": F-0485, Q-0278). Answer `FreepsChooseYes()`, dismiss
the hand shown (`FreepsDismissRevealedCards()`) and choose the minion to discard from it (a choice of no minion or
one, asked even with one minion there: Q-0278; `FreepsDeclineChoosing()` discards none); `FreepsChooseNo()` ends
the event with the minion returned. With no minion in that discard pile nothing is asked. The erratum 67_15 takes 2 and reveals unasked (its
text has no "may"). No printed card PLAYS a card from another player's pile (searched for 94_30).

**A card that answers a card DRAWN, and one that answers a card drawn or taken into hand; a draw at will; a card
taken into hand that is not drawn, from the discard pile and from the draw deck.** Morgul on the March 7_195,
Verily I Come 2_94; Elrond, Lord of Rivendell 1_40; Barliman Butterbur 1_70 with Mysterious Wizard 1_78, Master
Proudfoot 1_301. March: unique [wraith] support-area condition (to play, spot a Nazgûl: cheat it in), adds a threat
each time the Free Peoples player draws outside the regroup phase; a threat wants a companion to count it. Elrond:
ally, vitality 4, "Fellowship: Exert Elrond to draw a card" (his other texts act at the start of a turn and on his
play). Barliman: ally, vitality 2, "Fellowship: Exert Barliman Butterbur to take a [gandalf] event into hand from
your discard pile"; the Wizard is a [gandalf] skirmish event for the pile. A card taken from the draw deck that is
not drawn: Master Proudfoot 1_301 (ally; "Fellowship: If the twilight pool has fewer than 3 twilight tokens, add
(2) to reveal the top 3 cards of your draw deck. Take all [shire] cards revealed into hand and discard the rest";
`DismissRevealedCards()` after it). The card that answers either is Verily I Come 2_94, on the Ring-bearer, a
burden for each in the fellowship phase (F-0376; mechanics/deck/DrawnOrTakenIntoHandTests, Card_07_195_Tests).
The Shadow player's take from his draw deck: Fool of a Took! 1_170 ([moria] Shadow event; spot a Hobbit other than
the Ring-bearer; reveals from the top until a card that is no [moria] minion and takes them all into hand). Its
reveal is shown to BOTH players, so `DismissRevealedCards()`, not one side's, before the take (Card_03_029_Tests).

**A Free Peoples card that has the SHADOW player draw, with no choice in it.** Drawing His Eye 10_29; What Are We
Waiting For? 2_15. Drawing His Eye: [gondor] Fellowship event, cost 2; one card of the Shadow hand revealed at
random for each [gondor] companion spotted (Aragorn 1_89), a revealed Shadow card discarded, then "its owner draws
a card" (`drawCards`, `player: shadow`). A Shadow hand of one card makes the reveal and the discard certain.
What Are We Waiting For?: [dwarven] Fellowship event, cost 1; exert a Dwarf "to make an opponent draw 2 cards", who
then discards 2 Shadow cards from hand or skips his next Shadow phase. As built, each is the Shadow player's own draw
(Q-0334, open; Card_02_094_Tests.TheShadowPlayerDrawingInTheFellowshipPhaseAddsNoBurden).

**A hand revealed by its own owner, on each side (the invalid doer and the invalid hand for a bar on looking at or
revealing a hand).** Troll's Keyward, Keeper of the Beast 12_105; Mallorn-trees 1_54. Keyward: unique [orc] Orc,
cost 3, strength 8, vitality 3, site 4, lurker; "Shadow: Exert Troll's Keyward and reveal your hand to add (1) for
each [orc] Troll revealed": the Shadow player reveals his OWN hand and the Free Peoples player is shown it.
Mallorn-trees: [elven] support-area condition, cost 1; "Fellowship: Reveal an [elven] card from hand and place it
beneath your draw deck": one card of the Free Peoples player's own hand, shown to the Shadow player; not offered
with no [elven] card in hand (F-0420). A look at the Shadow hand by no card of the Free Peoples side is Galadriel's
Glade 1_351 (site 6 of `FellowshipSites`: `SkipToSite(6)` and an Elf companion). `Card_02_044_Tests`.

**A Free Peoples card that has the SHADOW player reveal his own hand; one card of the Shadow hand revealed at
random.** It Burns Us 6_110; Meant to Be Alone 3_109. It Burns Us: [shire] Regroup event, cost 1; spot Sam and
Sméagol or Gollum (Sméagol, Once Like Us 103_116 cheated in) "to choose an opponent who must reveal his or her
hand", then wounds a minion once for each culture revealed (no minion, nothing wounded). The game treats it as the
Free Peoples player's reveal (shapes.md, Q-0251: B). Meant to Be Alone: [shire] Fellowship event, cost 1; spot a
Hobbit companion who is not the Ring-bearer; shows one card at random and removes (X), its twilight cost: the
reveal with the least side effect. Lay six different cards and read which was shown (pitfalls, "A random or
whole-hand effect"). `Card_05_096_Tests`.

**A Shadow card stacked on a condition where play can put it; one event that discards a condition or a minion.**
Teeth of Mordor 5_110; Stand Against Darkness 1_63. Teeth: [sauron] support-area condition, "Regroup: Spot a
[sauron] Orc and remove (3) to stack that minion here". Stand Against Darkness: [elven] Maneuver event, cost 1,
"Exert an Elf to discard a [sauron] minion, a [sauron] condition, or a [sauron] possession". With one Mordor Guard
7_287 ("When this minion is discarded, remove 3 threats") stacked and another in play, two copies of the event show
that a stacked card was not discarded from play (mechanics/wounds/LeavingPlayTests).

**A [dwarven] condition that play stacks Free Peoples cards on.** Here Is Good Rock 4_50. Unique tale in the
support area, cost 1: "Skirmish: Stack a Free Peoples card from hand here to make a Dwarf damage +1".
`StackCardsOn(rock, <any Free Peoples card>)` is a board play reaches. For the same event stacked on a condition of
ANOTHER culture: I Would Have Gone With You to the End 101_23 ([gondor], PC set V1; play stacks any skirmish event
of its owner's there; with no Aragorn in play it does nothing).

**A card stacked by legal play: a Free Peoples card from hand; a minion from play.** The Shards of Narsil 3_44;
Teeth of Mordor 5_110. Shards: unique [gondor] artifact in the support area, "Fellowship: Stack a [gondor] card
from hand here. Fellowship: Add (1) to take a card stacked here into hand" (two actions: take one by its word,
`FreepsUseCardAction(shards, "Stack")`). Teeth: [sauron] support-area condition, "Regroup: Spot a [sauron] Orc and
remove (3) to stack that minion here"; its Shadow ability plays one back. A minion whose standing text reads off
another card, to show it out of force on the stack: Grishnákh 5_100 beside Orc Chieftain 1_266
(`GetMinionSiteNumber` 3, then 6) (mechanics/inplay/StackingTests).

**A reconcile in the middle of a phase.** Careful Study 7_19. [elven] Regroup event, cost 1: "Spot an Elf and
discard 3 cards from hand to reconcile your hand." With five other cards in hand: `FreepsChooseCards` of three,
then the reconcile's own 0-to-1 discard and the draw to 8 (mechanics/game/ReconcilingTests).

**A companion's twilight cost raised (to show the size of a discount on a cheap companion).** The Misadventure of
Mr. Underhill 1_129. [isengard] support-area condition, cost 2 ("To play, exert an Uruk-hai": cheat it into the
Shadow player's support area before the game). "The twilight cost of each companion played to a site which is not
a sanctuary is +2": at Bag End a companion of cost 2 reads 4 in hand, and 2 with a discount of 2, where a discount
of 3 would read 1 (`Card_15_064_Tests`; pitfalls, "A twilight cost never reads below 0").

**A Shadow possession's twilight cost raised (to show the size of a discount on a weapon of cost 0 or 1).** Taurnil
5_13 bearing Elven Bow 1_41. Taurnil: [elven] Elf companion ("To play, spot an Elf": cheat him in), "While Taurnil
bears a ranged weapon, the twilight cost of each Shadow possession is +2"; he then adds nothing to the fellowship
archery total. A Goblin Spear 1_182 (cost 1) reads 3, and 2 at "twilight cost -1", so a pool of exactly 2 is
offered and left at 0, where -2 would leave 1 and a pool of 1 withholds the play (`Card_01_193_Tests`).

**A Shadow condition's twilight cost raised (to show the size of a discount on a cheap condition).** Rohirrim Road
7_332, a King-block site 1: "Fellowship: Exert an Elf to make the twilight cost of each condition and possession +2
until the end of the turn", both sides' cards. Uruk-hai Armory 1_157 (cost 1) then reads 3 in the Shadow hand
(`Card_07_332_Tests`, on a King-block path). It would show No Business of Ours 52_44's -3, which its file still
shows with an ad hoc `TwilightCostModifier` (approved in the errata phase).

**A card in a hand whose cost a site 1 lowers (the CR 4.2 "twilight cost" example read by a Fellowship event on
turn 1).** Edoras Hall 7_330, the site 1 of `KingSites`: "The twilight cost of each [rohan] possession is -1".
Sturdy Shield 15_141 ([rohan] possession, printed 2) in the Shadow hand reads 1 there, and a text that reads "the
twilight cost of the card revealed" reads 2 (Card_03_016_Tests). Weatherhills 1_335 (Card_03_043_Tests) is site 2:
the turns `SkipToSite(2)` plays reconcile cards into the Shadow hand, which a random reveal then takes.

## Sites and moves

**Two sites whose standing texts read as one number.** Weatherhills 1_335; Ford of Bruinen 1_338; The Witch-king,
Lord of Angmar 1_237. At 2, "Each Nazgûl's twilight cost is -1"; at 3 (a sanctuary), "The twilight cost of the
first Nazgûl played to Ford of Bruinen each turn is -5". `GetTwilightCost` of the Witch-king (cost 8; his own text
is about strength) in the Shadow player's hand reads 8 with neither text in force, 7, 3, or 2 with both
(mechanics/sites/SiteTextHoldsOnlyWhereTheFellowshipIsTests). Weatherhills is also the one site-2 text a FELLOWSHIP phase
can read (no Fellowship-block site 2 has a fellowship action): for "your site N" with the owner's site of another
number as the invalid candidate (Hobbit Farmer 1_295), a Nazgûl in the mover's hand keeps her printed cost while
the fellowship is at site 1 and Weatherhills lies ahead (Card_01_295_Tests).

**An underground sanctuary whose Shadow number a discount can lower ("each underground site's Shadow number is
-2").** Caves of Aglarond 4_352: Towers site 6 (`TowersSites`), underground and a sanctuary, Shadow number 3, so
-2 reads 1. The Dimholt 8_117 (King site 3) is the underground sanctuary for a terrain word, but its Shadow number
of 1 floors at 0 under -2 (Card_02_121_Tests.AnUndergroundSanctuaryIsShadowNumberMinus2Too).

**A character at a Fellowship site 2 who is not in the fellowship ("only Hobbits are in the fellowship", "each Hobbit
moves to").** Goldberry 9_51 and Tom Bombadil 9_52 are the only allies who are no Hobbit with home 2F (Bounder 1_286
is the Hobbit one): an ally is no member of the fellowship and does not move with it (Card_01_330_Tests, Card_01_332_Tests).

**A card that reads the terrain of the fellowship's site (to show a site has gained or lost one).** Duilin, Ranger
From Blackroot Vale 14_7. Unique [gondor] Man, cost 2, strength 5, vitality 3, Ranger; "While the fellowship is at
a forest or river site, Duilin is strength +3" is his whole text, so 5 or 8 reads the site. A keyword on a site
that is NOT the fellowship's is read with `HasKeyword(site, Keyword.RIVER)` (`Card_17_120_Tests`).

**A river at a low site number ("moves from a river", "at a river" with moves left after it).** Buckleberry Ferry
1_330 (site 2, river; no assignment or skirmish phases while only Hobbits are companions) and Ford of Bruinen 1_338
(site 3, river and sanctuary; the first Nazgûl played there each turn is -5), on a Fellowship path of their own
with Bag End 1_319 at 1; `FellowshipSites` has rivers only at 7 and 8, where a second move runs into site 9. The two
differ in the sanctuary, so they are a valid spread for "a river" (`Card_01_046_Tests`).

**A turn with THREE moves, so that the same minions are on the table at two moves ("each time the fellowship moves"
with minions to act on; no minion is in play at the move that ends a fellowship phase).** Rivendell Waterfall
1_342, as site 3 of `FellowshipSites`, with a ranger. Forest, sanctuary: "While you can spot a ranger at Rivendell
Waterfall, the move limit is +1 for this turn". With a ranger among the companions (Aragorn 1_89 or 4_364):
`FreepsPass()`, minions cheated in, `SkipToMovementDecision()`, `FreepsChooseToMove()` (to the Waterfall;
`GetMoveLimit()` is 3 once the Shadow actions wait), `SkipToMovementDecision()`, `FreepsChooseToMove()` again
(`Card_04_364_Tests`, `Card_01_342_Tests`).

**A site taken under control, and one liberated, by legal play; a card that counts "each site you control"; a
battleground to control.** Southron Explorer 4_250; Théoden, Son of Thengel 4_292 with Guma 4_277; Uruk-hai Band
4_202; Uruk Camp 4_335. Explorer ([raider] Man, strength 5, vitality 3): "Regroup: Exert this minion and remove (5)
to take control of a site", twice in a phase with (10); two copies take two sites. Théoden (strength 6, vitality
2): "Regroup: Spot a [rohan] ally and exert Théoden to liberate a site"; Guma is the ally (discarded only if an
opponent controls site 4 of a Towers path). The Band is strength 10 and +2 for each site its owner controls: 10,
12, 14 reads the count for one player. Uruk Camp is a battleground and plains printed as site 2 of the Towers block
(`putAll(TowersSites); put("site2", "4_335")`): Advance Uruk Patrol 4_136 is strength +4 while its owner controls
it. For a board that only needs a site in someone's hands, the cheat (`ShadowTakeControlOfSite()`,
`FreepsTakeControlOfSite()`): never on player 1's first turn, where player 2's marker is still on site 1 and the cheat
finds no site even with the fellowship at site 2; `StartGame(); SkipToSite(2);` puts player 1's second turn at site 3
with site 1 behind every marker (Card_08_095_Tests). Pitfalls,
"A site is controlled lowest first"; mechanics/sites/ControlOfSitesTests.

**The next site played, and a site replaced, by legal play.** Pathfinder 1_110; Úlairë Nelya, Lieutenant of Morgul
1_233; Úlairë Nelya, Third of the Nine Riders 11_222. Pathfinder: [gondor] Fellowship or Regroup event, cost 0,
"Spot a ranger to play the fellowship's next site (replacing opponent's site if necessary)": on a numbered path it
lays the player's own site of the next number, or nothing where that site already lies; on a Shadows-block path he
chooses, and a site lying next is replaced. Nelya 1_233 ("Shadow: Exert ... and spot an opponent's site to replace
it with your site of the same number") replaces site 1 on the first turn; Nelya 11_222 ("Shadow: Exert ... to
replace the fellowship's current site with a site from your adventure deck") any site, on a Shadows-block path.
Duilin 14_7 reads the terrain under the fellowship before and after (mechanics/sites/PlayingSitesTests).
By a SITE's own text, a doer that is no card of the player's side: Mere of Dead Faces 11_246 ("Regroup: Exert
your Gollum or your Sméagol to play the fellowship's next site"), with Sméagol, Slinker 5_29 in play and the
fellowship there: `FreepsUseCardAction(site)`, `FreepsChooseCardBPFromSelection(next)`. A Free Peoples site played
that is NOT the next: There's Another Way 12_40 ([gollum] condition, spot Sméagol; "Regroup: Discard this condition
to replace the fellowship's current site"). Speak "Friend" and Enter 2_26 (spot Gandalf; Fellowship or Regroup)
also draws a card if the site played is underground (Abandoned Mine Shaft 13_185, `site2` of `ShadowsSites`, is):
choose another (`Card_13_164_Tests`).
A Free Peoples card that replaces a CHOSEN site of the fellowship's region, wherever in it the site lies: Traveled
Leader 12_34 ([gandalf] Maneuver or Regroup event, cost 1, "Spot a [gandalf] Wizard to replace a site in the
fellowship's current region with a site from your adventure deck"). The next site laid by a card that is no
ranger's: One Good Turn Deserves Another 11_49 ([gollum] Fellowship or Regroup event, cost 0, spot Sméagol; then
"you may add a burden to take this card back into hand", asked each time). Both in `Card_15_193_Tests`.
Under a bar on replacing sites (Rapid Reload 15_89's, Doorway to Doom 68_134's), Traveled Leader still asks "Choose
site to replace" among every site of the region and is played, nothing replaced, its site left in the adventure deck;
Pathfinder and Mere of Dead Faces 11_246 stay playable or usable and do nothing (Mere's exertion paid) (`Card_15_089_Tests`).

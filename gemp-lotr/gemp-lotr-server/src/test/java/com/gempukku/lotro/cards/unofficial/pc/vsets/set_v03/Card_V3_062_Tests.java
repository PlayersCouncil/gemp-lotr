package com.gempukku.lotro.cards.unofficial.pc.vsets.set_v03;

import com.gempukku.lotro.common.*;
import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_V3_062_Tests
{
	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("howdah1", "103_62");
					put("howdah2", "103_62");
					put("howdah3", "103_62");
					put("charger", "103_41");     // Bladetusk Charger: Maneuver: stack Southron Men from play on it to make it a fierce mounted Southron minion (4 + 4 a Southron, vitality 4) until the end of the turn
					put("matriarch", "103_42");   // Bladetusk Matriarch: Maneuver: remove (6) to make it a fierce mounted Southron minion (6 + 3 a Southron, vitality 6); any [raider] Man may be stacked on it
					put("mumak", "5_73");         // Mumak: mount, bearer must be a Southron; bearer is fierce
					put("steed", "1_208");        // Black Steed: mount, bearer must be a Nazgul
					put("lemenya", "1_232");      // Ulaire Lemenya: a Nazgul, to be mounted and no Southron
					put("rider", "4_222");        // Desert Warrior: Southron Man, strength 6, vitality 2; an optional trigger at the start of its own skirmish
					put("walker", "4_222");
					put("scout", "4_252");        // Southron Scout: Southron Man, ambush (2) and nothing else
					put("stalker", "103_49");     // Desert Wind Stalker: Southron. Tracker. "Southrons gain ambush (1)."
					put("axeman", "4_224");       // Easterling Axeman: a [raider] Man who is no Southron. Easterling. Strength +3 and fierce while 2 burdens can be spotted
					put("savage", "4_17");        // Dunlending Savage: a [dunland] Man with no text
					put("khamul", "103_61");      // Ulaire Attea, Khamul of the East: a [raider] minion who is no Man
					put("lord", "4_219");         // Desert Lord: Southron. Archer. An archery ability
					put("fanatic", "10_49");      // Southron Fanatic: a required trigger
					put("fighter", "7_131");      // Desert Fighter: an optional trigger with a cost
					put("traveler", "5_76");      // Southron Traveler: a response
					put("aragorn", "1_89");       // Aragorn, Ranger of the North: a Man, strength 8, vitality 4; Maneuver: exert Aragorn to make him defender +1
					put("takeitdown", "103_89");  // Take it Down!: Skirmish event: exert Theoden or Eomer to wound a mounted minion twice, then may exert X [rohan] companions to discard X stacked Shadow cards
					put("theoden", "7_255");      // Theoden, Rekindled King: strength 7, vitality 3; a fellowship ability never used here
					put("guard", "5_83");         // Household Guard: a [rohan] companion, vitality 3; valiant and nothing else
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void WarHowdahStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: V3
		 * Name: War Howdah
		 * Unique: false
		 * Side: Shadow
		 * Culture: Raider
		 * Twilight Cost: 2
		 * Type: Possession
		 * Subtype: Support Area
		 * Game Text: Bearer gains <b>archer</b>.
		 * 	Maneuver: If this is in your support area, exert a mounted Southron to transfer this to that Southron.
		 * 	Limit 1 per bearer. Choose a Man stacked on that Southron.  Until the end of the turn, that Southron
		 * 	gains the game text of that stacked Man.
		 */

		var scn = GetScenario();

		var card = scn.GetFreepsCard("howdah1");

		assertEquals("War Howdah", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, card.getBlueprint().getSide());
		assertEquals(Culture.RAIDER, card.getBlueprint().getCulture());
		assertEquals(CardType.POSSESSION, card.getBlueprint().getCardType());
		assertTrue(scn.HasKeyword(card, Keyword.SUPPORT_AREA));
		assertFalse(scn.HasKeyword(card, Keyword.ARCHER));
		assertEquals(2, card.getBlueprint().getTwilightCost());
	}

	@Test
	public void IsPlayedForTwoToTheSupportAreaAndGivesNobodyArcherThere() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var howdah = scn.GetShadowCard("howdah1");
		var rider = scn.GetShadowCard("rider");
		var mumak = scn.GetShadowCard("mumak");
		scn.MoveCardsToHand(howdah);

		scn.StartGame();
		scn.MoveMinionsToTable(rider);
		scn.AttachCardsTo(rider, mumak);
		scn.SetTwilight(8);
		scn.FreepsPass();

		// 8 set + 1 for Frodo + 2 for the shadow number of site 2
		assertEquals(11, scn.GetTwilight());
		assertInZone(Zone.HAND, howdah);
		assertTrue(scn.ShadowPlayAvailable(howdah));

		scn.ShadowPlayCard(howdah);

		// 11 - 2 to play
		assertEquals(9, scn.GetTwilight());
		assertInZone(Zone.SUPPORT, howdah);
		assertNotAttachedTo(howdah, rider);
		// With no bearer it makes nobody an archer, a mounted Southron on the table or not.
		assertFalse(scn.HasKeyword(rider, Keyword.ARCHER));
		// Its own ability is for the maneuver phase.
		assertFalse(scn.ShadowActionAvailable(howdah));
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void ManeuverActionExertsAMountedSouthronAndTransfersTheHowdahToThatSouthron() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var howdah = scn.GetShadowCard("howdah1");
		var howdah2 = scn.GetShadowCard("howdah2");
		var charger = scn.GetShadowCard("charger");
		var scout = scn.GetShadowCard("scout");
		var rider = scn.GetShadowCard("rider");
		var walker = scn.GetShadowCard("walker");
		var mumak = scn.GetShadowCard("mumak");
		var lemenya = scn.GetShadowCard("lemenya");
		var steed = scn.GetShadowCard("steed");
		var aragorn = scn.GetFreepsCard("aragorn");

		scn.MoveCardsToSupportArea(howdah, howdah2, charger);
		scn.MoveCompanionsToTable(aragorn);

		scn.StartGame();
		scn.MoveMinionsToTable(scout, rider, walker, lemenya);
		// A Southron mounted by a possession, and a mounted minion who is no Southron
		scn.AttachCardsTo(rider, mumak);
		scn.AttachCardsTo(lemenya, steed);
		scn.SkipToPhase(Phase.MANEUVER);
		scn.FreepsPass();

		// A Southron that is mounted by its own text
		scn.ShadowUseCardAction(charger);
		scn.ShadowChooseCards(scout);
		assertTrue(scn.HasKeyword(charger, Keyword.MOUNTED));
		scn.FreepsPass();

		int twilight = scn.GetTwilight();
		assertInZone(Zone.SUPPORT, howdah);
		assertEquals(0, scn.GetWoundsOn(rider));
		assertEquals(0, scn.GetWoundsOn(charger));
		assertFalse(scn.HasKeyword(rider, Keyword.ARCHER));
		assertTrue(scn.ShadowActionAvailable(howdah));

		scn.ShadowUseCardAction(howdah);

		// Mounted Southrons, however they come to be mounted. Not the Southron on foot, not the mounted Nazgul.
		assertTrue(scn.ShadowHasCardChoiceAvailable(charger, rider));
		assertFalse(scn.ShadowHasCardChoiceAvailable(walker));
		assertFalse(scn.ShadowHasCardChoiceAvailable(lemenya));
		assertEquals(1, scn.ShadowGetChoiceMin());

		scn.ShadowChooseCard(rider);

		assertEquals(1, scn.GetWoundsOn(rider));
		assertEquals(0, scn.GetWoundsOn(charger));
		assertAttachedTo(howdah, rider);
		assertNotAttachedTo(howdah, charger);
		// A transfer by a card's own effect costs nothing.
		assertEquals(twilight, scn.GetTwilight());

		// The bearer is an archer, and nobody else: not the other mounted Southron, and the copy still in the
		// support area gives it to no one.
		assertTrue(scn.HasKeyword(rider, Keyword.ARCHER));
		assertFalse(scn.HasKeyword(charger, Keyword.ARCHER));
		assertFalse(scn.HasKeyword(walker, Keyword.ARCHER));
		assertInZone(Zone.SUPPORT, howdah2);

		// Nothing is stacked on a Southron who rides a Mumak, so there is no Man to choose.
		assertTrue(scn.AwaitingFreepsManeuverPhaseActions());

		// Once it is borne its ability is gone, while the copy in the support area still offers its own to the
		// Charger, who can exert.
		scn.FreepsPass();
		assertFalse(scn.ShadowActionAvailable(howdah));
		assertTrue(scn.ShadowActionAvailable(howdah2));
		assertTrue(scn.AwaitingShadowManeuverPhaseActions());
	}

	@Test
	public void IsNotOfferedOnceTheOnlyMountedSouthronIsExhausted() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var howdah = scn.GetShadowCard("howdah1");
		var rider = scn.GetShadowCard("rider");
		var walker = scn.GetShadowCard("walker");
		var mumak = scn.GetShadowCard("mumak");
		var aragorn = scn.GetFreepsCard("aragorn");

		scn.MoveCardsToSupportArea(howdah);
		scn.MoveCompanionsToTable(aragorn);

		scn.StartGame();
		scn.MoveMinionsToTable(rider, walker);
		scn.AttachCardsTo(rider, mumak);
		scn.SkipToPhase(Phase.MANEUVER);

		// Aragorn's own maneuver ability keeps the phase going between the Shadow player's two decisions.
		scn.FreepsUseCardAction(aragorn);
		assertEquals(0, scn.GetWoundsOn(rider));
		assertTrue(scn.ShadowActionAvailable(howdah));

		// Vitality 2 with 1 wound: the mounted Southron can no longer exert. The Southron on foot can, and
		// is no use.
		scn.AddWoundsToChar(rider, 1);
		scn.ShadowPass();
		scn.FreepsUseCardAction(aragorn);

		assertEquals(1, scn.GetWoundsOn(rider));
		assertEquals(0, scn.GetWoundsOn(walker));
		assertFalse(scn.ShadowActionAvailable(howdah));
		assertInZone(Zone.SUPPORT, howdah);
		assertTrue(scn.AwaitingShadowManeuverPhaseActions());
	}

	@Test
	public void ASouthronBearsOnlyOneHowdah() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var howdah1 = scn.GetShadowCard("howdah1");
		var howdah2 = scn.GetShadowCard("howdah2");
		var howdah3 = scn.GetShadowCard("howdah3");
		var charger = scn.GetShadowCard("charger");
		var scout = scn.GetShadowCard("scout");
		var rider = scn.GetShadowCard("rider");
		var mumak = scn.GetShadowCard("mumak");
		var aragorn = scn.GetFreepsCard("aragorn");

		scn.MoveCardsToSupportArea(howdah1, howdah2, howdah3, charger);
		scn.MoveCompanionsToTable(aragorn);

		scn.StartGame();
		scn.MoveMinionsToTable(scout, rider);
		scn.AttachCardsTo(rider, mumak);
		scn.SkipToPhase(Phase.MANEUVER);
		scn.FreepsPass();

		scn.ShadowUseCardAction(charger);
		scn.ShadowChooseCards(scout);
		scn.FreepsPass();

		// The first Howdah may go to either mounted Southron.
		scn.ShadowUseCardAction(howdah1);
		assertTrue(scn.ShadowHasCardChoiceAvailable(charger, rider));
		scn.ShadowChooseCard(charger);
		assertAttachedTo(howdah1, charger);
		assertEquals(1, scn.GetWoundsOn(charger));
		scn.FreepsPass();

		// The second has one Southron left to go to, and goes there without asking: the Charger, vitality 4
		// with 1 wound, could exert again but already bears one.
		assertTrue(scn.ShadowActionAvailable(howdah2));
		scn.ShadowUseCardAction(howdah2);
		assertAttachedTo(howdah2, rider);
		assertNotAttachedTo(howdah2, charger);
		assertEquals(1, scn.GetWoundsOn(rider));
		assertEquals(1, scn.GetWoundsOn(charger));
		scn.FreepsPass();

		// The third has nowhere to go, though the Charger can still exert.
		assertFalse(scn.ShadowActionAvailable(howdah3));
		assertInZone(Zone.SUPPORT, howdah3);
		assertAttachedTo(howdah1, charger);
		assertTrue(scn.AwaitingShadowManeuverPhaseActions());
	}

	@Test
	public void ChoosesAManStackedOnThatSouthronWhoGainsThatMansGameTextAndNoOthers() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var howdah = scn.GetShadowCard("howdah1");
		var matriarch = scn.GetShadowCard("matriarch");
		var charger = scn.GetShadowCard("charger");
		var stalker = scn.GetShadowCard("stalker");
		var axeman = scn.GetShadowCard("axeman");
		var savage = scn.GetShadowCard("savage");
		var khamul = scn.GetShadowCard("khamul");
		var scout = scn.GetShadowCard("scout");
		var aragorn = scn.GetFreepsCard("aragorn");

		scn.MoveCardsToSupportArea(howdah, matriarch, charger);
		scn.MoveCompanionsToTable(aragorn);

		scn.StartGame();
		// On the Matriarch: a Southron Man, a [raider] Man who is no Southron, a Man who is not [raider], and a
		// [raider] minion who is no Man.
		scn.StackCardsOn(matriarch, stalker, axeman, savage, khamul);
		scn.MoveMinionsToTable(scout);
		scn.AddBurdens(2);
		scn.SetTwilight(10);
		scn.SkipToPhase(Phase.MANEUVER);
		scn.FreepsPass();

		scn.ShadowUseCardAction(matriarch);
		scn.FreepsPass();
		// The Scout is the one Southron Man in play, and goes onto the other mounted Southron.
		scn.ShadowUseCardAction(charger);
		scn.FreepsPass();

		// 6 + 3 for the one Southron stacked on her
		assertEquals(9, scn.GetStrength(matriarch));
		assertFalse(scn.HasKeyword(matriarch, Keyword.EASTERLING));
		assertFalse(scn.HasKeyword(matriarch, Keyword.TRACKER));
		assertEquals(1, scn.GetKeywordCount(matriarch, Keyword.AMBUSH));
		// 4 + 4 for the Scout
		assertEquals(8, scn.GetStrength(charger));
		assertEquals(1, scn.GetKeywordCount(charger, Keyword.AMBUSH));

		scn.ShadowUseCardAction(howdah);
		assertTrue(scn.ShadowHasCardChoiceAvailable(matriarch, charger));
		scn.ShadowChooseCard(matriarch);

		// Any Man stacked on her; not the Nazgul stacked there, not the Man stacked on the Charger.
		assertTrue(scn.ShadowHasCardChoiceAvailable(stalker, axeman, savage));
		assertFalse(scn.ShadowHasCardChoiceAvailable(khamul));
		assertFalse(scn.ShadowHasCardChoiceAvailable(scout));
		assertEquals(1, scn.ShadowGetChoiceMin());
		assertEquals(1, scn.ShadowGetChoiceMax());

		scn.ShadowChooseCard(axeman);

		assertAttachedTo(howdah, matriarch);
		assertInZone(Zone.STACKED, axeman);
		// The Axeman's text: "Easterling. While you can spot 2 burdens, this minion is strength +3 and fierce."
		assertTrue(scn.HasKeyword(matriarch, Keyword.EASTERLING));
		// 9 + 3 with 2 burdens
		assertEquals(12, scn.GetStrength(matriarch));
		// Nothing of the Stalker's, who was not chosen: no tracker, and no ambush for the Southrons.
		assertFalse(scn.HasKeyword(matriarch, Keyword.TRACKER));
		assertEquals(1, scn.GetKeywordCount(matriarch, Keyword.AMBUSH));
		assertEquals(1, scn.GetKeywordCount(charger, Keyword.AMBUSH));
		// The other Southron gains nothing.
		assertFalse(scn.HasKeyword(charger, Keyword.EASTERLING));
		assertEquals(8, scn.GetStrength(charger));

		// It is the Axeman's text she has, condition and all: with one burden the +3 is gone.
		scn.RemoveBurdens(1);
		assertEquals(9, scn.GetStrength(matriarch));
		assertTrue(scn.AwaitingFreepsManeuverPhaseActions());
	}

	@Test
	public void GainedKeywordsAndModifierLastThroughASecondMoveUntilTheTurnEnds() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var howdah = scn.GetShadowCard("howdah1");
		var charger = scn.GetShadowCard("charger");
		var stalker = scn.GetShadowCard("stalker");
		var walker = scn.GetShadowCard("walker");
		var aragorn = scn.GetFreepsCard("aragorn");

		scn.MoveCardsToSupportArea(howdah, charger);
		scn.MoveCompanionsToTable(aragorn);

		scn.StartGame();
		scn.MoveMinionsToTable(stalker, walker);
		scn.SkipToPhase(Phase.MANEUVER);
		scn.FreepsPass();

		scn.ShadowUseCardAction(charger);
		scn.ShadowChooseCards(stalker);
		scn.FreepsPass();

		assertFalse(scn.HasKeyword(charger, Keyword.TRACKER));
		assertEquals(1, scn.GetKeywordCount(charger, Keyword.AMBUSH));
		assertFalse(scn.HasKeyword(walker, Keyword.AMBUSH));

		// The Charger is the one mounted Southron and the Stalker the one Man stacked on it.
		scn.ShadowUseCardAction(howdah);

		assertAttachedTo(howdah, charger);
		assertEquals(1, scn.GetWoundsOn(charger));
		// The Stalker's text: "Southron. Tracker. ... Southrons gain ambush (1)."
		assertTrue(scn.HasKeyword(charger, Keyword.TRACKER));
		assertEquals(1, scn.GetKeywordCount(walker, Keyword.AMBUSH));
		// 1 of its own for the Southron stacked on it + 1 as a Southron
		assertEquals(2, scn.GetKeywordCount(charger, Keyword.AMBUSH));
		assertTrue(scn.AwaitingFreepsManeuverPhaseActions());

		// The Charger is an archer now: its one arrow goes to Aragorn.
		scn.SkipToArcheryWounds();
		scn.FreepsChooseCard(aragorn);

		// The regroup phase of the first move
		scn.SkipToMovementDecision();
		assertEquals(Phase.REGROUP, scn.GetCurrentPhase());
		assertTrue(scn.HasKeyword(charger, Keyword.TRACKER));
		assertEquals(1, scn.GetKeywordCount(walker, Keyword.AMBUSH));

		// The second move: the same turn.
		scn.FreepsChooseToMove();
		assertEquals(Phase.SHADOW, scn.GetCurrentPhase());
		assertAttachedTo(howdah, charger);
		assertTrue(scn.HasKeyword(charger, Keyword.ARCHER));
		assertTrue(scn.HasKeyword(charger, Keyword.TRACKER));
		assertEquals(1, scn.GetKeywordCount(walker, Keyword.AMBUSH));
		assertEquals(2, scn.GetKeywordCount(charger, Keyword.AMBUSH));

		// The turn ends: the Charger is discarded as a minion, and what it bore and held with it.
		scn.SkipToArcheryWounds();
		scn.FreepsChooseCard(aragorn);
		scn.SkipToPhase(Phase.REGROUP);
		assertTrue(scn.HasKeyword(charger, Keyword.TRACKER));
		scn.PassCurrentPhaseActions();
		scn.ShadowDeclineReconciliation();

		assertInDiscard(charger);
		assertInDiscard(howdah);
		assertInDiscard(stalker);
		assertTrue(scn.ShadowDecisionAvailable("Play Fellowship action or Pass"));
	}

	@Test
	public void GainsTheStackedMansKeywordWithItsNumber() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var frodo = scn.GetRingBearer();
		var howdah = scn.GetShadowCard("howdah1");
		var charger = scn.GetShadowCard("charger");
		var scout = scn.GetShadowCard("scout");
		var aragorn = scn.GetFreepsCard("aragorn");

		scn.MoveCardsToSupportArea(howdah, charger);
		scn.MoveCompanionsToTable(aragorn);

		scn.StartGame();
		scn.MoveMinionsToTable(scout);
		scn.SkipToPhase(Phase.MANEUVER);
		scn.FreepsPass();

		// The Scout is the one Southron Man in play, then the one Man stacked on the Charger.
		scn.ShadowUseCardAction(charger);
		scn.FreepsPass();

		// The Charger's own ambush (1) for the one Southron stacked on it
		assertEquals(1, scn.GetKeywordCount(charger, Keyword.AMBUSH));

		scn.ShadowUseCardAction(howdah);
		assertAttachedTo(howdah, charger);

		// The Scout's text: "Southron. Ambush (2)."
		// 1 of its own + 2
		assertEquals(3, scn.GetKeywordCount(charger, Keyword.AMBUSH));

		// The Charger's arrow goes to Frodo.
		scn.SkipToArcheryWounds();
		scn.FreepsChooseCard(frodo);
		scn.PassCurrentPhaseActions();

		int twilight = scn.GetTwilight();
		scn.FreepsAssignToMinions(aragorn, charger);
		// The two instances of ambush are offered as one, for their sum.
		assertTrue(scn.ShadowHasOptionalTriggerAvailable());
		scn.ShadowAcceptOptionalTrigger();

		// (1) + (2)
		assertEquals(twilight + 3, scn.GetTwilight());
		assertTrue(scn.FreepsDecisionAvailable("Choose next skirmish to resolve"));
	}

	@Test
	public void GainsTheStackedMansPhaseAbility() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var howdah = scn.GetShadowCard("howdah1");
		var charger = scn.GetShadowCard("charger");
		var lord = scn.GetShadowCard("lord");
		var aragorn = scn.GetFreepsCard("aragorn");

		scn.MoveCardsToSupportArea(howdah, charger);
		scn.MoveCompanionsToTable(aragorn);

		scn.StartGame();
		scn.MoveMinionsToTable(lord);
		scn.SkipToPhase(Phase.MANEUVER);
		scn.FreepsPass();

		// The Desert Lord is the one Southron Man in play, then the one Man stacked on the Charger.
		scn.ShadowUseCardAction(charger);
		scn.FreepsPass();
		scn.ShadowUseCardAction(howdah);
		assertAttachedTo(howdah, charger);

		scn.PassCurrentPhaseActions();
		assertEquals(Phase.ARCHERY, scn.GetCurrentPhase());
		scn.FreepsPass();

		// "Archery: Exert Desert Lord to exert a companion (except the Ring-bearer); Desert Lord does not add
		// to the minion archery total."
		assertEquals(1, scn.GetWoundsOn(charger));
		assertEquals(0, scn.GetWoundsOn(aragorn));
		assertEquals(1, scn.GetShadowArcheryTotal());
		assertTrue(scn.ShadowActionAvailable(charger));

		scn.ShadowUseCardAction(charger);

		// Aragorn is the only companion who is not the Ring-bearer.
		assertEquals(2, scn.GetWoundsOn(charger));
		assertEquals(1, scn.GetWoundsOn(aragorn));
		// The Charger is the one the text now speaks of: the archer the Howdah made of it no longer counts.
		assertEquals(0, scn.GetShadowArcheryTotal());
		assertTrue(scn.AwaitingFreepsArcheryPhaseActions());
	}

	@Test
	public void GainsTheStackedMansRequiredTrigger() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var frodo = scn.GetRingBearer();
		var howdah = scn.GetShadowCard("howdah1");
		var charger = scn.GetShadowCard("charger");
		var fanatic = scn.GetShadowCard("fanatic");
		var aragorn = scn.GetFreepsCard("aragorn");

		scn.MoveCardsToSupportArea(howdah, charger);
		scn.MoveCompanionsToTable(aragorn);

		scn.StartGame();
		scn.MoveMinionsToTable(fanatic);
		scn.SkipToPhase(Phase.MANEUVER);
		scn.FreepsPass();

		scn.ShadowUseCardAction(charger);
		scn.FreepsPass();
		scn.ShadowUseCardAction(howdah);
		assertAttachedTo(howdah, charger);

		// The Charger's arrow goes to Frodo, so that Aragorn comes to the assignment unwounded.
		scn.SkipToArcheryWounds();
		scn.FreepsChooseCard(frodo);
		scn.PassCurrentPhaseActions();
		assertEquals(0, scn.GetWoundsOn(aragorn));

		// "Each time a Man is assigned to skirmish this minion, wound that Man."
		scn.FreepsAssignToMinions(aragorn, charger);
		// The Charger's own ambush (1)
		scn.ShadowDeclineOptionalTrigger();

		assertEquals(1, scn.GetWoundsOn(aragorn));
		assertTrue(scn.FreepsDecisionAvailable("Choose next skirmish to resolve"));
	}

	@Test
	public void GainsTheStackedMansOptionalTrigger() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var frodo = scn.GetRingBearer();
		var howdah = scn.GetShadowCard("howdah1");
		var charger = scn.GetShadowCard("charger");
		var fighter = scn.GetShadowCard("fighter");
		var aragorn = scn.GetFreepsCard("aragorn");

		scn.MoveCardsToSupportArea(howdah, charger);
		scn.MoveCompanionsToTable(aragorn);

		scn.StartGame();
		scn.MoveMinionsToTable(fighter);
		scn.SetTwilight(10);
		scn.SkipToPhase(Phase.MANEUVER);
		scn.FreepsPass();

		scn.ShadowUseCardAction(charger);
		scn.FreepsPass();
		scn.ShadowUseCardAction(howdah);
		assertAttachedTo(howdah, charger);

		scn.SkipToArcheryWounds();
		scn.FreepsChooseCard(frodo);
		scn.PassCurrentPhaseActions();
		scn.FreepsAssignToMinions(aragorn, charger);
		// The Charger's own ambush (1)
		scn.ShadowDeclineOptionalTrigger();

		int twilight = scn.GetTwilight();
		assertEquals(0, scn.GetThreats());
		scn.FreepsResolveSkirmish(aragorn);

		// The Charger's own text adds a threat as its skirmish starts; the Fighter's is the offer that follows:
		// "At the start of each skirmish involving this minion, you may remove (1) to add a threat."
		assertEquals(1, scn.GetThreats());
		assertTrue(scn.ShadowHasOptionalTriggerAvailable(charger));

		scn.ShadowAcceptOptionalTrigger();

		assertEquals(twilight - 1, scn.GetTwilight());
		assertEquals(2, scn.GetThreats());
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
	}

	@Test
	public void GainsTheStackedMansResponse() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var frodo = scn.GetRingBearer();
		var howdah = scn.GetShadowCard("howdah1");
		var charger = scn.GetShadowCard("charger");
		var traveler = scn.GetShadowCard("traveler");
		var walker = scn.GetShadowCard("walker");
		var aragorn = scn.GetFreepsCard("aragorn");

		scn.MoveCardsToSupportArea(howdah, charger);
		scn.MoveCompanionsToTable(aragorn);

		scn.StartGame();
		scn.MoveMinionsToTable(traveler, walker);
		scn.SetTwilight(10);
		scn.SkipToPhase(Phase.MANEUVER);
		scn.FreepsPass();

		scn.ShadowUseCardAction(charger);
		scn.ShadowChooseCards(traveler);
		scn.FreepsPass();
		scn.ShadowUseCardAction(howdah);
		assertAttachedTo(howdah, charger);

		scn.SkipToArcheryWounds();
		scn.FreepsChooseCard(frodo);
		scn.PassCurrentPhaseActions();
		// Aragorn (8) against the Desert Warrior (6): another Southron is about to take a wound.
		scn.FreepsAssignToMinions(aragorn, walker);
		scn.ShadowDeclineAssignments();
		scn.FreepsResolveSkirmish(aragorn);
		// The Desert Warrior's own trigger at the start of its skirmish
		scn.ShadowDeclineOptionalTrigger();

		int twilight = scn.GetTwilight();
		assertEquals(0, scn.GetWoundsOn(walker));
		scn.PassCurrentPhaseActions();

		// "Response: If a Southron is about to take a wound, remove (3) to prevent that wound."
		assertTrue(scn.ShadowHasOptionalTriggerAvailable(charger));
		scn.ShadowChooseOptionalTrigger(charger);

		assertEquals(twilight - 3, scn.GetTwilight());
		assertEquals(0, scn.GetWoundsOn(walker));
		assertInZone(Zone.SHADOW_CHARACTERS, walker);
		assertTrue(scn.AwaitingFreepsAssignmentPhaseActions());
	}

	@Test
	public void KeepsTheMansResponseAndKeywordWithItsNumberWhenHeIsDiscardedFromTheStack() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var frodo = scn.GetRingBearer();
		var howdah = scn.GetShadowCard("howdah1");
		var charger = scn.GetShadowCard("charger");
		var traveler = scn.GetShadowCard("traveler");
		var takeitdown = scn.GetFreepsCard("takeitdown");
		var theoden = scn.GetFreepsCard("theoden");
		var guard = scn.GetFreepsCard("guard");

		scn.MoveCardsToSupportArea(howdah, charger);
		scn.MoveCompanionsToTable(theoden, guard);
		scn.MoveCardsToHand(takeitdown);

		scn.StartGame();
		scn.MoveMinionsToTable(traveler);
		scn.SetTwilight(20);
		scn.SkipToPhase(Phase.MANEUVER);
		scn.FreepsPass();

		// The Traveler is the one Southron Man in play, then the one Man stacked on the Charger.
		scn.ShadowUseCardAction(charger);
		scn.FreepsPass();
		scn.ShadowUseCardAction(howdah);
		assertAttachedTo(howdah, charger);
		assertEquals(1, scn.GetWoundsOn(charger));

		// The Traveler's text: "Southron. Ambush (2). Response: If a Southron is about to take a wound, remove (3)
		// to prevent that wound."
		// 1 of its own for the Southron stacked on it + 2
		assertEquals(3, scn.GetKeywordCount(charger, Keyword.AMBUSH));

		// The Charger's arrow goes to Frodo.
		scn.SkipToArcheryWounds();
		scn.FreepsChooseCard(frodo);
		scn.PassCurrentPhaseActions();
		scn.FreepsAssignToMinions(theoden, charger);
		// Its ambush
		scn.ShadowDeclineOptionalTrigger();
		scn.FreepsResolveSkirmish(theoden);

		// Theoden exerts and the Charger, the one mounted minion, is wounded twice; the response it has from the
		// Traveler is offered for each wound and declined.
		scn.FreepsPlayCard(takeitdown);
		assertTrue(scn.ShadowHasOptionalTriggerAvailable(charger));
		scn.ShadowDeclineOptionalTrigger();
		assertTrue(scn.ShadowHasOptionalTriggerAvailable(charger));
		scn.ShadowDeclineOptionalTrigger();
		assertEquals(3, scn.GetWoundsOn(charger));

		// The Guard exerts and the one stacked Shadow card is discarded: the Man is gone from the stack.
		assertInZone(Zone.STACKED, traveler);
		scn.FreepsChooseCards(guard);
		assertEquals(1, scn.GetWoundsOn(guard));
		assertInZone(Zone.DISCARD, traveler);
		assertEquals(0, scn.GetStackedCards(charger).size());

		// The Charger has lost its own ambush (1), with no Southron stacked on it, and keeps the Traveler's (2).
		assertEquals(2, scn.GetKeywordCount(charger, Keyword.AMBUSH));

		// Theoden (7) beats the Charger (4, with no Southron stacked on it), which has 3 wounds and a vitality
		// of 4. It still has the Traveler's response, and is a Southron about to take a wound.
		int twilight = scn.GetTwilight();
		scn.ShadowPass();
		scn.FreepsPass();
		assertTrue(scn.ShadowHasOptionalTriggerAvailable(charger));
		scn.ShadowChooseOptionalTrigger(charger);

		assertEquals(twilight - 3, scn.GetTwilight());
		assertEquals(3, scn.GetWoundsOn(charger));
		assertInZone(Zone.SHADOW_CHARACTERS, charger);

		// The Charger is fierce: assigned again, it adds (2) for the ambush it has from the Traveler.
		scn.PassCurrentPhaseActions();
		twilight = scn.GetTwilight();
		scn.FreepsAssignToMinions(theoden, charger);
		assertTrue(scn.ShadowHasOptionalTriggerAvailable());
		scn.ShadowAcceptOptionalTrigger();

		assertEquals(twilight + 2, scn.GetTwilight());
		assertTrue(scn.FreepsDecisionAvailable("Choose next skirmish to resolve"));
	}
}

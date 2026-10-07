package com.gempukku.lotro.cards.unofficial.pc.vsets.set_v03;

import com.gempukku.lotro.common.CardType;
import com.gempukku.lotro.common.Keyword;
import com.gempukku.lotro.common.Phase;
import com.gempukku.lotro.common.SitesBlock;
import com.gempukku.lotro.common.Zone;
import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.game.LotroCardBlueprint;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_V3_127_Tests
{
	// The Ring-bearer is Frodo, Son of Drogo (vitality 4), who bears The Ruling Ring.
	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("aragorn", "1_89");        // Aragorn, Ranger of the North: [gondor] Man, a ranger, cost 4, strength 8, vitality 4; "Maneuver: Exert Aragorn to make him defender +1."
					put("gandalf", "1_72");        // Gandalf, Friend of the Shirefolk: a Wizard, vitality 4
					put("gimli", "1_13");          // Gimli, Son of Gloin: vitality 3
					put("merry", "1_302");         // Merry, Friend to Sam: an unbound companion costing 1
					put("sam", "2_114");           // Sam, Proper Poet: vitality 4; "Maneuver: Exert Sam twice to make him defender +1 until the regroup phase."
					put("orophin", "1_56");        // Orophin, Lorien Bowman: Elf ally, vitality 3; "Archery: Exert Orophin to wound an Uruk-hai."
					put("aid", "6_9");             // Lend Us Your Aid: maneuver event; "Exert Gimli twice to make him and each Elf companion strength +3 until the regroup phase."

					// The Sooner We're Rid of It: condition in the support area. "Skirmish: Exert a companion and
					// hinder each card borne by them to make that companion strength +1 for each card hindered
					// (+3 if that card is a weapon). Hinder this condition (or discard this if you can spot more
					// than 4 companions)."
					put("sooner", "103_118");
					put("sword", "1_112");         // Ranger's Sword: possession, hand weapon
					put("athelas", "1_94");        // Athelas: possession of no class
					put("alliance", "1_49");       // The Last Alliance of Elves and Men: condition borne by a [gondor] Man
					put("armor", "1_8");           // Dwarven Armor: possession, on another companion
					put("enter", "103_9");         // You Cannot Enter Here: maneuver event, "Exert your Wizard to hinder a Shadow card. Exhaust that Wizard if you chose a minion."
					// Northern Signal-fire, Flame of Eilenach (two may be in play): possession in the support area.
					// "Skirmish: Hinder this beacon to make your unbound Man strength +1 for each of the
					// following you can spot: ranger, knight, mounted Man, valiant Man, ring-bound Man,
					// exhausted Man."
					put("fire1", "103_29");
					put("fire2", "103_29");

					put("initiate", "103_47");     // Desert Wind Initiate: cost 1; "When you play this minion, remove (3) or hinder this minion."
					put("savage", "1_151");        // Uruk Savage: [isengard] Uruk-hai, strength 5, vitality 3
					put("runner1", "1_178");       // Goblin Runner: strength 5, vitality 1
					put("runner2", "1_178");
					put("witchking", "1_237");     // The Witch-king, Lord of Angmar: unique [ringwraith] minion, strength 14, vitality 4
					put("scimitar", "1_180");      // Goblin Scimitar: Shadow possession, strength +2, put on a minion by the rig
					put("hides", "4_19");          // Hides: Shadow possession in the support area
					put("reach", "1_137");         // Saruman's Reach: maneuver event; exert an Uruk-hai to make the opponent exert 2 companions or put on The One Ring
					put("knows", "103_40");        // A Wizard Knows Better: maneuver event; "... you may exert X [isengard] minions ... to hinder an unbound companion costing up to X."
					put("dear", "103_104");        // Was He Dear to Thee?: maneuver event; "Exert a unique [sauron] or [ringwraith] minion and hinder the Ring-bearer to make each minion strength +2 until the regroup phase."
				}},
				new HashMap<>()
				{{
					put("site1", "7_330");
					put("site2", "7_335");
					put("site3", "8_117");
					put("site4", "7_342");
					put("site5", "7_345");
					put("site6", "7_350");
					put("site7", "8_120");
					put("site8", "7_356");         // Cross Roads: a Shadow ability that needs 2 [raider] Men
					put("site9", "103_127");       // Doom Causeway
				}},
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	// The same Ring-bearer with The One Ring, The Binding Ring 9_1 (vitality +1), which has no response to a
	// wound: The Ruling Ring's would be offered in the middle of the action the test is about.
	protected VirtualTableScenario GetBindingRingScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("aragorn", "1_89");        // Aragorn, Ranger of the North: the companion in the skirmish
					put("sooner", "103_118");      // The Sooner We're Rid of It: "Skirmish: Exert a companion and hinder each card borne by them ..."
					put("runner", "1_178");        // Goblin Runner
				}},
				new HashMap<>()
				{{
					put("site1", "7_330");
					put("site2", "7_335");
					put("site3", "8_117");
					put("site4", "7_342");
					put("site5", "7_345");
					put("site6", "7_350");
					put("site7", "8_120");
					put("site8", "7_356");
					put("site9", "103_127");       // Doom Causeway
				}},
				VirtualTableScenario.FOTRFrodo,
				"9_1"
		);
	}

	@Test
	public void DoomCausewayStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: V3
		 * Name: Doom Causeway
		 * Unique: False
		 * Type: Site
		 * Block: King
		 * Site Number: 9
		 * Shadow Number: 9
		 * Direction: Left
		 * Game Text: <b>Mountain</b>. <b>Underground</b>. <b>Battleground</b>. Each time a card is hindered,
		 * 	discard it. Each time a character is exerted, wound it.
		 */

		var scn = GetScenario();

		var card = scn.GetFreepsSite(9);

		assertEquals("Doom Causeway", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(CardType.SITE, card.getBlueprint().getCardType());
		assertEquals(SitesBlock.KING, card.getBlueprint().getSiteBlock());
		assertEquals(9, card.getBlueprint().getSiteNumber());
		assertEquals(9, card.getBlueprint().getTwilightCost());
		assertEquals(LotroCardBlueprint.Direction.LEFT, card.getBlueprint().getSiteDirection());
		assertTrue(scn.HasKeyword(card, Keyword.MOUNTAIN));
		assertTrue(scn.HasKeyword(card, Keyword.UNDERGROUND));
		assertTrue(scn.HasKeyword(card, Keyword.BATTLEGROUND));
	}

	@Test
	public void CardsHinderedTogetherAreEachDiscardedAndSoIsTheConditionThatHinderedThem() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var frodo = scn.GetRingBearer();
		var aragorn = scn.GetFreepsCard("aragorn");
		var gimli = scn.GetFreepsCard("gimli");
		var sooner = scn.GetFreepsCard("sooner");
		var sword = scn.GetFreepsCard("sword");
		var athelas = scn.GetFreepsCard("athelas");
		var alliance = scn.GetFreepsCard("alliance");
		var armor = scn.GetFreepsCard("armor");
		var runner = scn.GetShadowCard("runner1");

		scn.StartGame();
		scn.SkipToSite(8);
		scn.MoveCompanionsToTable(aragorn, gimli);
		scn.MoveCardsToSupportArea(sooner);
		// On Aragorn a weapon, a possession of no class and a condition; on Gimli an armor.
		scn.AttachCardsTo(aragorn, sword, athelas, alliance);
		scn.AttachCardsTo(gimli, armor);
		scn.MoveMinionsToTable(runner);

		scn.FreepsPass();
		assertEquals("Doom Causeway", scn.GetCurrentSite().getBlueprint().getTitle());
		scn.SkipToAssignments();
		scn.FreepsAssignAndResolve(aragorn, runner);

		assertEquals(0, scn.GetWoundsOn(aragorn));
		assertAttachedTo(sword, aragorn);
		assertAttachedTo(athelas, aragorn);
		assertAttachedTo(alliance, aragorn);
		assertInZone(Zone.SUPPORT, sooner);
		assertTrue(scn.FreepsActionAvailable(sooner));

		scn.FreepsUseCardAction(sooner);
		assertTrue(scn.FreepsHasCardChoiceAvailable(frodo, aragorn, gimli));
		scn.FreepsChooseCard(aragorn);

		// Each of the three cards he bore was hindered, and each is discarded.
		assertInDiscard(sword, athelas, alliance);
		// The condition then hindered itself, and is discarded as well.
		assertInDiscard(sooner);
		// He exerted for it: the exertion and the site's wound.
		assertEquals(2, scn.GetWoundsOn(aragorn));

		// What was not hindered is where it was.
		assertAttachedTo(armor, gimli);
		assertFalse(scn.IsHindered(armor));
		assertEquals(0, scn.GetWoundsOn(gimli));
		assertEquals(0, scn.GetWoundsOn(frodo));
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());
	}

	@Test
	public void AMinionThatHindersItselfWhenPlayedIsDiscarded() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var initiate = scn.GetShadowCard("initiate");
		var savage = scn.GetShadowCard("savage");

		scn.StartGame();
		scn.SkipToSite(8);
		scn.MoveCardsToHand(initiate);
		scn.MoveMinionsToTable(savage);

		scn.FreepsPass();
		assertEquals("Doom Causeway", scn.GetCurrentSite().getBlueprint().getTitle());
		// 1 companion + 9 for the site
		assertEquals(10, scn.GetTwilight());

		assertTrue(scn.ShadowPlayAvailable(initiate));
		scn.ShadowPlayCard(initiate);
		// In play, and asked to remove (3) or hinder it.
		assertInZone(Zone.SHADOW_CHARACTERS, initiate);
		assertFalse(scn.IsHindered(initiate));
		scn.ShadowChoose("Hinder");

		assertInDiscard(initiate);
		// 10 - 1 to play it; nothing was removed for its text.
		assertEquals(9, scn.GetTwilight());
		// The minion beside it was not hindered and stays.
		assertInZone(Zone.SHADOW_CHARACTERS, savage);
		assertEquals(0, scn.GetWoundsOn(savage));
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void AShadowCardHinderedByAFreePeoplesEventIsDiscardedAndNoOtherShadowCardIs() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var gandalf = scn.GetFreepsCard("gandalf");
		var enter = scn.GetFreepsCard("enter");
		var savage = scn.GetShadowCard("savage");
		var scimitar = scn.GetShadowCard("scimitar");
		var hides = scn.GetShadowCard("hides");

		scn.StartGame();
		scn.SkipToSite(8);
		scn.MoveCompanionsToTable(gandalf);
		scn.MoveCardsToHand(enter);
		scn.MoveMinionsToTable(savage);
		scn.AttachCardsTo(savage, scimitar);
		scn.MoveCardsToSupportArea(hides);

		scn.FreepsPass();
		assertEquals("Doom Causeway", scn.GetCurrentSite().getBlueprint().getTitle());
		scn.SkipToPhase(Phase.MANEUVER);

		assertEquals(0, scn.GetWoundsOn(gandalf));
		// 5 + 2 for the Scimitar
		assertEquals(7, scn.GetStrength(savage));
		assertTrue(scn.FreepsPlayAvailable(enter));
		scn.FreepsPlayCard(enter);
		// Gandalf, the only Wizard, exerts; any Shadow card may be hindered.
		assertTrue(scn.FreepsHasCardChoiceAvailable(savage, scimitar, hides));
		scn.FreepsChooseCard(scimitar);

		// The possession the minion bore was hindered and is discarded.
		assertInDiscard(scimitar);
		assertEquals(5, scn.GetStrength(savage));
		// Its bearer and the possession in the support area were not hindered.
		assertInZone(Zone.SHADOW_CHARACTERS, savage);
		assertFalse(scn.IsHindered(savage));
		assertEquals(0, scn.GetWoundsOn(savage));
		assertInZone(Zone.SUPPORT, hides);
		assertFalse(scn.IsHindered(hides));
		// Gandalf's exertion and the site's wound.
		assertEquals(2, scn.GetWoundsOn(gandalf));
		assertTrue(scn.AwaitingShadowManeuverPhaseActions());
	}

	@Test
	public void AHinderedCompanionIsPlacedInTheDiscardPile() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var frodo = scn.GetRingBearer();
		var aragorn = scn.GetFreepsCard("aragorn");
		var merry = scn.GetFreepsCard("merry");
		var savage = scn.GetShadowCard("savage");
		var knows = scn.GetShadowCard("knows");

		scn.StartGame();
		scn.SkipToSite(8);
		scn.MoveCompanionsToTable(aragorn, merry);
		scn.MoveMinionsToTable(savage);
		scn.MoveCardsToHand(knows);

		scn.FreepsPass();
		assertEquals("Doom Causeway", scn.GetCurrentSite().getBlueprint().getTitle());
		scn.SkipToPhase(Phase.MANEUVER);
		scn.FreepsPass();

		assertInZone(Zone.FREE_CHARACTERS, merry);
		assertTrue(scn.ShadowPlayAvailable(knows));
		scn.ShadowPlayCard(knows);
		// Nothing to restore. The one [isengard] minion exerts, X is 1, and Merry is the one unbound
		// companion costing 1 or less.
		scn.ShadowChooseYes();

		assertInDiscard(merry);
		assertNotInZone(Zone.DEAD, merry);
		// The Savage's exertion and the site's wound.
		assertEquals(2, scn.GetWoundsOn(savage));
		// The companions who were not hindered stay.
		assertInZone(Zone.FREE_CHARACTERS, aragorn, frodo);
		assertFalse(scn.IsHindered(aragorn));
		assertTrue(scn.AwaitingFreepsManeuverPhaseActions());
	}

	@Test
	public void ACardHinderedAtSite8IsStillInPlayHereAndTheCopyHinderedHereIsDiscarded() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var aragorn = scn.GetFreepsCard("aragorn");
		var fire1 = scn.GetFreepsCard("fire1");
		var fire2 = scn.GetFreepsCard("fire2");
		var runner1 = scn.GetShadowCard("runner1");
		var runner2 = scn.GetShadowCard("runner2");

		scn.StartGame();
		scn.SkipToSite(7);
		scn.MoveCompanionsToTable(aragorn);
		scn.MoveCardsToSupportArea(fire1, fire2);
		scn.MoveMinionsToTable(runner1);

		scn.FreepsPass();
		assertEquals(8, scn.GetCurrentSiteNumber());
		scn.SkipToAssignments();
		scn.FreepsAssignAndResolve(aragorn, runner1);
		assertEquals(8, scn.GetStrength(aragorn));
		scn.FreepsUseCardAction(fire1);

		// Site 8: the beacon is hindered and stays where it lies. Aragorn is a ranger: 8 + 1.
		assertTrue(scn.IsHindered(fire1));
		assertInZone(Zone.SUPPORT, fire1);
		assertEquals(9, scn.GetStrength(aragorn));
		scn.PassSkirmishActions();

		scn.SkipToMovementDecision();
		scn.FreepsChooseToMove();
		assertEquals("Doom Causeway", scn.GetCurrentSite().getBlueprint().getTitle());
		// Arriving with a card already hindered discards nothing.
		assertTrue(scn.IsHindered(fire1));
		assertInZone(Zone.SUPPORT, fire1);

		scn.MoveMinionsToTable(runner2);
		scn.SkipToAssignments();
		scn.FreepsAssignAndResolve(aragorn, runner2);
		assertEquals(8, scn.GetStrength(aragorn));
		assertInZone(Zone.SUPPORT, fire2);
		assertFalse(scn.IsHindered(fire2));
		assertTrue(scn.FreepsActionAvailable(fire2));
		scn.FreepsUseCardAction(fire2);

		// Here the beacon that hindered itself is discarded; its cost was paid, so Aragorn is 8 + 1.
		assertInDiscard(fire2);
		assertEquals(9, scn.GetStrength(aragorn));
		// The one hindered at site 8 is still in play, still hindered.
		assertTrue(scn.IsHindered(fire1));
		assertInZone(Zone.SUPPORT, fire1);
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());
	}

	@Test
	public void TheRingBearerHinderedHereStaysInPlay() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var frodo = scn.GetRingBearer();
		var witchking = scn.GetShadowCard("witchking");
		var savage = scn.GetShadowCard("savage");
		var dear = scn.GetShadowCard("dear");

		scn.StartGame();
		scn.SkipToSite(8);
		scn.MoveMinionsToTable(witchking, savage);
		scn.MoveCardsToHand(dear);

		scn.FreepsPass();
		assertEquals("Doom Causeway", scn.GetCurrentSite().getBlueprint().getTitle());
		scn.SkipToPhase(Phase.MANEUVER);
		scn.FreepsPass();

		assertFalse(scn.IsHindered(frodo));
		assertEquals(14, scn.GetStrength(witchking));
		assertEquals(5, scn.GetStrength(savage));
		assertTrue(scn.ShadowPlayAvailable(dear));
		// The Witch-king is the only unique [sauron] or [ringwraith] minion, and exerts.
		scn.ShadowPlayCard(dear);

		// The Ring-bearer cannot be discarded: he is hindered, and still in play.
		assertTrue(scn.IsHindered(frodo));
		assertInZone(Zone.FREE_CHARACTERS, frodo);
		assertEquals(frodo, scn.GetRingBearer());
		assertFalse(scn.GameIsFinished());
		// The Witch-king's exertion and the site's wound.
		assertEquals(2, scn.GetWoundsOn(witchking));
		// The event's costs were paid: each minion is strength +2.
		assertEquals(16, scn.GetStrength(witchking));
		assertEquals(7, scn.GetStrength(savage));
		assertTrue(scn.AwaitingFreepsManeuverPhaseActions());
	}

	@Test
	public void TheOneRingHinderedHereStaysOnItsBearerHindered() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetBindingRingScenario();

		var frodo = scn.GetRingBearer();
		var ring = scn.GetRing();
		var aragorn = scn.GetFreepsCard("aragorn");
		var sooner = scn.GetFreepsCard("sooner");
		var runner = scn.GetShadowCard("runner");

		scn.StartGame();
		scn.SkipToSite(8);
		scn.MoveCompanionsToTable(aragorn);
		scn.MoveCardsToSupportArea(sooner);
		scn.MoveMinionsToTable(runner);

		scn.FreepsPass();
		assertEquals("Doom Causeway", scn.GetCurrentSite().getBlueprint().getTitle());
		scn.SkipToAssignments();
		scn.FreepsAssignAndResolve(aragorn, runner);

		assertAttachedTo(ring, frodo);
		assertFalse(scn.IsHindered(ring));
		scn.FreepsUseCardAction(sooner);
		assertTrue(scn.FreepsHasCardChoiceAvailable(frodo, aragorn));
		// The Ring-bearer exerts, and each card he bears is hindered: The One Ring.
		scn.FreepsChooseCard(frodo);

		// The Ring is a card that was hindered here, and The One Ring is never discarded: it is where it was,
		// hindered.  The condition, which hindered itself, went as any other card does.
		assertTrue(scn.IsHindered(ring));
		assertAttachedTo(ring, frodo);
		assertInZone(Zone.ATTACHED, ring);
		assertEquals(frodo, scn.GetRingBearer());
		assertInDiscard(sooner);
		// His exertion and the site's wound.
		assertEquals(2, scn.GetWoundsOn(frodo));
		assertInZone(Zone.FREE_CHARACTERS, frodo);
		assertFalse(scn.GameIsFinished());
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());
	}

	@Test
	public void ACompanionWhoExertsHereTakesAWoundAsWellAndAtSite8DoesNot() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var frodo = scn.GetRingBearer();
		var aragorn = scn.GetFreepsCard("aragorn");
		var savage = scn.GetShadowCard("savage");

		scn.StartGame();
		scn.SkipToSite(7);
		scn.MoveCompanionsToTable(aragorn);
		scn.MoveMinionsToTable(savage);

		scn.FreepsPass();
		assertEquals(8, scn.GetCurrentSiteNumber());
		scn.SkipToPhase(Phase.MANEUVER);
		assertEquals(0, scn.GetWoundsOn(aragorn));
		scn.FreepsUseCardAction(aragorn);
		// Site 8: the exertion and nothing more.
		assertEquals(1, scn.GetWoundsOn(aragorn));

		scn.SkipToMovementDecision();
		scn.FreepsChooseToMove();
		assertEquals("Doom Causeway", scn.GetCurrentSite().getBlueprint().getTitle());
		scn.SkipToPhase(Phase.MANEUVER);
		assertEquals(1, scn.GetWoundsOn(aragorn));
		assertTrue(scn.FreepsActionAvailable(aragorn));
		scn.FreepsUseCardAction(aragorn);

		// Here: 1 from site 8, the exertion, and the site's wound.
		assertEquals(3, scn.GetWoundsOn(aragorn));
		assertTrue(scn.HasKeyword(aragorn, Keyword.DEFENDER));
		// Nobody else exerted.
		assertEquals(0, scn.GetWoundsOn(frodo));
		assertEquals(0, scn.GetWoundsOn(savage));
		assertTrue(scn.AwaitingShadowManeuverPhaseActions());
	}

	@Test
	public void ACharacterWhoExertsTwiceForOneCostIsWoundedTwice() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var frodo = scn.GetRingBearer();
		var sam = scn.GetFreepsCard("sam");
		var savage = scn.GetShadowCard("savage");

		scn.StartGame();
		scn.SkipToSite(8);
		scn.MoveCompanionsToTable(sam);
		scn.MoveMinionsToTable(savage);

		scn.FreepsPass();
		assertEquals("Doom Causeway", scn.GetCurrentSite().getBlueprint().getTitle());
		scn.SkipToPhase(Phase.MANEUVER);

		assertEquals(4, scn.GetVitality(sam));
		assertEquals(0, scn.GetWoundsOn(sam));
		assertTrue(scn.FreepsActionAvailable(sam));
		scn.FreepsUseCardAction(sam);

		// Two exertions and a wound for each: 4 on a vitality of 4. One wound would leave him alive; so would
		// an exertion in a wound's place, which an exhausted Sam could not make.
		assertInZone(Zone.DEAD, sam);
		assertEquals(0, scn.GetWoundsOn(frodo));
		assertFalse(scn.GameIsFinished());
		assertTrue(scn.AwaitingShadowManeuverPhaseActions());
	}

	@Test
	public void ACharacterWithVitality3WhoMustExertTwiceIsExhaustedAfterTheFirstAndTheEventDoesNothing() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var gimli = scn.GetFreepsCard("gimli");
		var aid = scn.GetFreepsCard("aid");
		var savage = scn.GetShadowCard("savage");

		scn.StartGame();
		scn.SkipToSite(8);
		scn.MoveCompanionsToTable(gimli);
		scn.MoveCardsToHand(aid);
		scn.MoveMinionsToTable(savage);

		scn.FreepsPass();
		assertEquals("Doom Causeway", scn.GetCurrentSite().getBlueprint().getTitle());
		scn.SkipToPhase(Phase.MANEUVER);

		assertEquals(3, scn.GetVitality(gimli));
		assertEquals(0, scn.GetWoundsOn(gimli));
		assertEquals(6, scn.GetStrength(gimli));
		// A vitality of 3 is enough to exert twice, so the event may be played.
		assertTrue(scn.FreepsPlayAvailable(aid));
		scn.FreepsPlayCard(aid);

		// The site's wound answers the first exertion before the second is made: 2 wounds on a vitality of 3,
		// and he cannot exert again. Both exertions made first and two wounds after them would have killed him.
		assertInZone(Zone.FREE_CHARACTERS, gimli);
		assertEquals(2, scn.GetWoundsOn(gimli));
		// The cost could not be paid in full: the event is spent and gives him nothing.
		assertInDiscard(aid);
		assertEquals(6, scn.GetStrength(gimli));
		assertTrue(scn.AwaitingShadowManeuverPhaseActions());
	}

	@Test
	public void AnAllyWhoExertsIsWoundedAndTheMinionHeWoundsTakesOnlyThatWound() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var orophin = scn.GetFreepsCard("orophin");
		var savage = scn.GetShadowCard("savage");

		scn.StartGame();
		scn.SkipToSite(8);
		scn.MoveAlliesToTable(orophin);
		scn.MoveMinionsToTable(savage);

		scn.FreepsPass();
		assertEquals("Doom Causeway", scn.GetCurrentSite().getBlueprint().getTitle());
		scn.SkipToPhase(Phase.ARCHERY);

		assertEquals(0, scn.GetWoundsOn(orophin));
		assertEquals(0, scn.GetWoundsOn(savage));
		assertTrue(scn.FreepsActionAvailable(orophin));
		// "Exert Orophin to wound an Uruk-hai": the Savage is the only one.
		scn.FreepsUseCardAction(orophin);

		// The ally exerted: his exertion and the site's wound.
		assertEquals(2, scn.GetWoundsOn(orophin));
		// The minion was wounded, not exerted: that wound and no more.
		assertEquals(1, scn.GetWoundsOn(savage));
		assertTrue(scn.AwaitingShadowArcheryPhaseActions());
	}

	@Test
	public void AMinionExertedAsACostAndCompanionsExertedByItsEffectAreEachWounded() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var frodo = scn.GetRingBearer();
		var aragorn = scn.GetFreepsCard("aragorn");
		var gandalf = scn.GetFreepsCard("gandalf");
		var savage = scn.GetShadowCard("savage");
		var witchking = scn.GetShadowCard("witchking");
		var reach = scn.GetShadowCard("reach");

		scn.StartGame();
		scn.SkipToSite(8);
		scn.MoveCompanionsToTable(aragorn, gandalf);
		scn.MoveMinionsToTable(savage, witchking);
		scn.MoveCardsToHand(reach);

		scn.FreepsPass();
		assertEquals("Doom Causeway", scn.GetCurrentSite().getBlueprint().getTitle());
		scn.SkipToPhase(Phase.MANEUVER);
		scn.FreepsPass();

		assertEquals(0, scn.GetWoundsOn(savage));
		assertTrue(scn.ShadowPlayAvailable(reach));
		// The Savage is the only Uruk-hai, and exerts.
		scn.ShadowPlayCard(reach);
		scn.FreepsChoose("Exert 2 companions");
		assertTrue(scn.FreepsHasCardChoiceAvailable(frodo, aragorn, gandalf));
		scn.FreepsChooseCards(aragorn, gandalf);

		// The minion that exerted as the cost: its exertion and the site's wound.
		assertEquals(2, scn.GetWoundsOn(savage));
		// Each companion exerted by the event: the same.
		assertEquals(2, scn.GetWoundsOn(aragorn));
		assertEquals(2, scn.GetWoundsOn(gandalf));
		// Those who did not exert have nothing.
		assertEquals(0, scn.GetWoundsOn(frodo));
		assertEquals(0, scn.GetWoundsOn(witchking));
		assertFalse(scn.RBWearingOneRing());
		assertTrue(scn.AwaitingFreepsManeuverPhaseActions());
	}
}

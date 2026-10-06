package com.gempukku.lotro.cards.unofficial.pc.vsets.set_v03;

import com.gempukku.lotro.common.*;
import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.game.PhysicalCardImpl;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import com.gempukku.lotro.logic.modifiers.IsAdditionalRaceModifier;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_V3_036_Tests
{
	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("shut1", "103_36");
					put("shut2", "103_36");
					put("deadman1", "10_27");   // Dead Man of Dunharrow: a [gondor] Wraith, strength 6, vitality 3, enduring; his text is about being played
					put("deadman2", "10_27");
					put("deadman3", "10_27");
					put("gimli", "1_13");       // Gimli, Son of Glóin: a [dwarven] Dwarf, made a Wraith where a test says so; a skirmish ability never used here
					put("knight", "5_35");      // Gondorian Knight: a [gondor] Man with no text, strength 6
					put("wall", "5_33");        // City Wall: a Free Peoples condition that a minion can come to bear; bearer is strength -3

					put("pinned", "1_192");     // Pinned Down: a [moria] condition in the support area; does nothing without a [moria] archer
					put("burden", "7_60");      // Heavy Burden: a [gollum] condition in the support area; a regroup ability that needs Gollum
					put("lether", "7_63");      // Let Her Deal With Them: a [gollum] condition borne by a minion, who is damage +1
					put("deceit", "18_29");     // Deceit: a [gollum] condition in the support area; a response that removes (1) to stop a Free Peoples card discarding another [gollum] condition
					put("ships", "8_65");       // Ships of Great Draught: a [raider] possession in the support area; Shadow and regroup abilities only
					put("savage", "1_151");     // Uruk Savage: strength 5, vitality 3; damage +1 is its whole text
					put("runner", "1_178");     // Goblin Runner: strength 5, vitality 1
					put("corpse", "6_99");      // Corpse Lights: a [sauron] minion who is a Wraith, strength 7, vitality 2; a skirmish ability the Shadow player never uses here
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void TheWayisShutStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: V3
		 * Name: The Way is Shut
		 * Unique: false
		 * Side: Free Peoples
		 * Culture: Gondor
		 * Twilight Cost: 1
		 * Type: Event
		 * Subtype: Maneuver, Skirmish
		 * Game Text: Maneuver: Spot 3 [gondor] Wraiths to discard a Shadow condition.  You may exert a Wraith to
		 * 	hinder another Shadow condition.
		 * 	Skirmish: Make your Wraith strength +1 for each threat you can spot (limit +3).
		 */

		var scn = GetScenario();

		var card = scn.GetFreepsCard("shut1");

		assertEquals("The Way is Shut", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.FREE_PEOPLE, card.getBlueprint().getSide());
		assertEquals(Culture.GONDOR, card.getBlueprint().getCulture());
		assertEquals(CardType.EVENT, card.getBlueprint().getCardType());
		assertTrue(scn.HasTimeword(card, Timeword.MANEUVER));
		assertTrue(scn.HasTimeword(card, Timeword.SKIRMISH));
		assertEquals(1, card.getBlueprint().getTwilightCost());
	}

	@Test
	public void ManeuverSpotsThreeGondorWraithsToDiscardAShadowConditionAndAWraithMayBeExertedToHinderAnother() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var shut = scn.GetFreepsCard("shut1");
		var deadman1 = scn.GetFreepsCard("deadman1");
		var deadman2 = scn.GetFreepsCard("deadman2");
		var deadman3 = scn.GetFreepsCard("deadman3");
		var gimli = scn.GetFreepsCard("gimli");
		var knight = scn.GetFreepsCard("knight");
		var wall = scn.GetFreepsCard("wall");
		var frodo = scn.GetRingBearer();
		var pinned = scn.GetShadowCard("pinned");
		var burden = scn.GetShadowCard("burden");
		var lether = scn.GetShadowCard("lether");
		var ships = scn.GetShadowCard("ships");
		var savage = scn.GetShadowCard("savage");
		// Exactly 3 [gondor] Wraiths, one of them with 1 vitality left; a [dwarven] companion made a Wraith; a [gondor] Man.
		scn.MoveCompanionsToTable(deadman1, deadman2, deadman3, gimli, knight);
		scn.ApplyAdHocModifier(new IsAdditionalRaceModifier(null, gimli, null, Race.WRAITH));
		scn.AddWoundsToChar(deadman3, 2);
		scn.MoveCardsToHand(shut);
		// Shadow conditions of two cultures in the support area, and a Shadow possession beside them.
		scn.MoveCardsToSupportArea(pinned, burden, ships);

		scn.StartGame();
		scn.MoveMinionsToTable(savage);
		// On the minion: a Shadow condition, and a condition that is the Free Peoples player's own.
		scn.AttachCardsTo(savage, lether, wall);
		// Threats are for the skirmish half, and change nothing here.
		scn.AddThreats(2);
		scn.SkipToPhase(Phase.MANEUVER);

		int twilight = scn.GetTwilight();
		assertEquals(6, scn.GetStrength(deadman1));
		assertEquals(6, scn.GetStrength(gimli));
		assertTrue(scn.FreepsPlayAvailable(shut));
		scn.FreepsPlayCard(shut);

		assertEquals(twilight + 1, scn.GetTwilight());
		assertTrue(scn.FreepsHasCardChoiceAvailable(pinned, burden, lether));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(ships, wall));
		assertEquals(1, scn.FreepsGetChoiceMin());
		scn.FreepsChooseCard(burden);

		assertInDiscard(burden);
		assertInZone(Zone.SUPPORT, pinned, ships);
		assertAttachedTo(lether, savage);
		assertAttachedTo(wall, savage);

		assertTrue(scn.FreepsDecisionAvailable("exert a Wraith"));
		scn.FreepsChooseYes();

		// A Wraith: the two [gondor] Wraiths able to exert and the [dwarven] one.
		assertTrue(scn.FreepsHasCardChoiceAvailable(deadman1, deadman2, gimli));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(deadman3, knight, frodo));
		assertEquals(1, scn.FreepsGetChoiceMin());
		assertEquals(0, scn.GetWoundsOn(gimli));
		scn.FreepsChooseCard(gimli);
		assertEquals(1, scn.GetWoundsOn(gimli));
		assertEquals(0, scn.GetWoundsOn(deadman1));
		assertEquals(0, scn.GetWoundsOn(deadman2));

		// Another Shadow condition: the two left, a [moria] one in the support area and a [gollum] one on the minion.
		assertTrue(scn.FreepsHasCardChoiceAvailable(pinned, lether));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(ships, wall));
		assertEquals(1, scn.FreepsGetChoiceMin());
		assertFalse(scn.IsHindered(lether));
		scn.FreepsChooseCard(lether);

		assertTrue(scn.IsHindered(lether));
		assertFalse(scn.IsHindered(pinned));
		assertFalse(scn.IsHindered(ships));
		assertFalse(scn.IsHindered(wall));
		assertAttachedTo(lether, savage);
		assertInZone(Zone.SUPPORT, pinned);
		assertInDiscard(shut);
		assertEquals(twilight + 1, scn.GetTwilight());
		assertEquals(6, scn.GetStrength(deadman1));
		// Gimli took a wound and is not enduring.
		assertEquals(6, scn.GetStrength(gimli));
		assertTrue(scn.AwaitingShadowManeuverPhaseActions());
	}

	@Test
	public void DecliningTheExertionHindersNothing() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var shut = scn.GetFreepsCard("shut1");
		var deadman1 = scn.GetFreepsCard("deadman1");
		var deadman2 = scn.GetFreepsCard("deadman2");
		var deadman3 = scn.GetFreepsCard("deadman3");
		var pinned = scn.GetShadowCard("pinned");
		var burden = scn.GetShadowCard("burden");
		var savage = scn.GetShadowCard("savage");
		scn.MoveCompanionsToTable(deadman1, deadman2, deadman3);
		scn.MoveCardsToHand(shut);
		scn.MoveCardsToSupportArea(pinned, burden);

		scn.StartGame();
		scn.MoveMinionsToTable(savage);
		scn.SkipToPhase(Phase.MANEUVER);

		scn.FreepsPlayCard(shut);
		scn.FreepsChooseCard(pinned);
		assertInDiscard(pinned);

		assertTrue(scn.FreepsDecisionAvailable("exert a Wraith"));
		scn.FreepsChooseNo();

		assertEquals(0, scn.GetWoundsOn(deadman1));
		assertEquals(0, scn.GetWoundsOn(deadman2));
		assertEquals(0, scn.GetWoundsOn(deadman3));
		assertFalse(scn.IsHindered(burden));
		assertInZone(Zone.SUPPORT, burden);
		assertInDiscard(shut);
		assertTrue(scn.AwaitingShadowManeuverPhaseActions());
	}

	@Test
	public void CannotBePlayedInTheManeuverPhaseWithTwoGondorWraiths() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var shut = scn.GetFreepsCard("shut1");
		var deadman1 = scn.GetFreepsCard("deadman1");
		var deadman2 = scn.GetFreepsCard("deadman2");
		var gimli = scn.GetFreepsCard("gimli");
		var knight = scn.GetFreepsCard("knight");
		var pinned = scn.GetShadowCard("pinned");
		var savage = scn.GetShadowCard("savage");
		// Two [gondor] Wraiths, a Wraith who is not [gondor], and a [gondor] companion who is no Wraith.
		scn.MoveCompanionsToTable(deadman1, deadman2, gimli, knight);
		scn.ApplyAdHocModifier(new IsAdditionalRaceModifier(null, gimli, null, Race.WRAITH));
		scn.MoveCardsToHand(shut);
		scn.MoveCardsToSupportArea(pinned);

		scn.StartGame();
		scn.MoveMinionsToTable(savage);
		scn.SkipToPhase(Phase.MANEUVER);

		assertTrue(scn.IsRace(gimli, Race.WRAITH));
		assertFalse(scn.FreepsPlayAvailable(shut));
		assertInZone(Zone.SUPPORT, pinned);
		assertTrue(scn.AwaitingFreepsManeuverPhaseActions());
	}

	@Test
	public void IsPlayableInTheManeuverPhaseAndNotInTheFellowshipArcheryAssignmentOrRegroupPhases() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var shut = scn.GetFreepsCard("shut1");
		var deadman1 = scn.GetFreepsCard("deadman1");
		var deadman2 = scn.GetFreepsCard("deadman2");
		var deadman3 = scn.GetFreepsCard("deadman3");
		var pinned = scn.GetShadowCard("pinned");
		var savage = scn.GetShadowCard("savage");
		// Everything either half could want: 3 [gondor] Wraiths, a Shadow condition, threats.
		scn.MoveCompanionsToTable(deadman1, deadman2, deadman3);
		scn.MoveCardsToHand(shut);
		scn.MoveCardsToSupportArea(pinned);
		scn.AddThreats(2);

		scn.StartGame();
		assertTrue(scn.AwaitingFellowshipPhaseActions());
		assertFalse(scn.FreepsPlayAvailable(shut));

		scn.MoveMinionsToTable(savage);
		scn.SkipToPhase(Phase.MANEUVER);
		assertTrue(scn.FreepsPlayAvailable(shut));

		scn.SkipToPhase(Phase.ARCHERY);
		assertTrue(scn.AwaitingFreepsArcheryPhaseActions());
		assertFalse(scn.FreepsPlayAvailable(shut));

		scn.SkipToPhase(Phase.ASSIGNMENT);
		assertTrue(scn.AwaitingFreepsAssignmentPhaseActions());
		assertFalse(scn.FreepsPlayAvailable(shut));

		scn.SkipToPhase(Phase.REGROUP);
		assertFalse(scn.FreepsPlayAvailable(shut));
		assertInHand(shut);
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void WithEveryWraithExhaustedNoExertionIsOffered() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var shut = scn.GetFreepsCard("shut1");
		var deadman1 = scn.GetFreepsCard("deadman1");
		var deadman2 = scn.GetFreepsCard("deadman2");
		var deadman3 = scn.GetFreepsCard("deadman3");
		var knight = scn.GetFreepsCard("knight");
		var pinned = scn.GetShadowCard("pinned");
		var burden = scn.GetShadowCard("burden");
		var savage = scn.GetShadowCard("savage");
		// Each Wraith has 1 vitality left; the Man beside them could exert.
		scn.MoveCompanionsToTable(deadman1, deadman2, deadman3, knight);
		scn.AddWoundsToChar(deadman1, 2);
		scn.AddWoundsToChar(deadman2, 2);
		scn.AddWoundsToChar(deadman3, 2);
		scn.MoveCardsToHand(shut);
		scn.MoveCardsToSupportArea(pinned, burden);

		scn.StartGame();
		scn.MoveMinionsToTable(savage);
		scn.SkipToPhase(Phase.MANEUVER);

		assertTrue(scn.FreepsPlayAvailable(shut));
		scn.FreepsPlayCard(shut);
		scn.FreepsChooseCard(pinned);

		assertInDiscard(pinned);
		assertFalse(scn.FreepsDecisionAvailable("exert a Wraith"));
		assertFalse(scn.IsHindered(burden));
		assertEquals(0, scn.GetWoundsOn(knight));
		assertEquals(2, scn.GetWoundsOn(deadman1));
		assertInDiscard(shut);
		assertTrue(scn.AwaitingShadowManeuverPhaseActions());
	}

	@Test
	public void WithNoOtherShadowConditionTheExertionIsStillOfferedAndHindersNothing() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var shut = scn.GetFreepsCard("shut1");
		var deadman1 = scn.GetFreepsCard("deadman1");
		var deadman2 = scn.GetFreepsCard("deadman2");
		var deadman3 = scn.GetFreepsCard("deadman3");
		var pinned = scn.GetShadowCard("pinned");
		var ships = scn.GetShadowCard("ships");
		var savage = scn.GetShadowCard("savage");
		scn.MoveCompanionsToTable(deadman1, deadman2, deadman3);
		scn.MoveCardsToHand(shut);
		// One Shadow condition, and a Shadow possession.
		scn.MoveCardsToSupportArea(pinned, ships);

		scn.StartGame();
		scn.MoveMinionsToTable(savage);
		scn.SkipToPhase(Phase.MANEUVER);

		scn.FreepsPlayCard(shut);
		// The only Shadow condition is discarded without asking which.
		assertInDiscard(pinned);

		// The exertion is a cost, and a cost that can be paid is offered whatever it will buy.
		assertTrue(scn.FreepsDecisionAvailable("exert a Wraith"));
		scn.FreepsChooseYes();
		assertTrue(scn.FreepsHasCardChoiceAvailable(deadman1, deadman2, deadman3));
		scn.FreepsChooseCard(deadman2);

		assertEquals(1, scn.GetWoundsOn(deadman2));
		assertEquals(0, scn.GetWoundsOn(deadman1));
		assertEquals(0, scn.GetWoundsOn(deadman3));
		assertFalse(scn.IsHindered(ships));
		assertInZone(Zone.SUPPORT, ships);
		assertInDiscard(shut);
		assertTrue(scn.AwaitingShadowManeuverPhaseActions());
	}

	@Test
	public void WithNoShadowConditionToDiscardItCanStillBePlayedAndTheExertionIsStillOffered() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var shut = scn.GetFreepsCard("shut1");
		var deadman1 = scn.GetFreepsCard("deadman1");
		var deadman2 = scn.GetFreepsCard("deadman2");
		var deadman3 = scn.GetFreepsCard("deadman3");
		var wall = scn.GetFreepsCard("wall");
		var ships = scn.GetShadowCard("ships");
		var savage = scn.GetShadowCard("savage");
		scn.MoveCompanionsToTable(deadman1, deadman2, deadman3);
		scn.MoveCardsToHand(shut);
		// A Shadow possession, and on the minion a condition that is the Free Peoples player's own: no Shadow condition anywhere.
		scn.MoveCardsToSupportArea(ships);

		scn.StartGame();
		scn.MoveMinionsToTable(savage);
		scn.AttachCardsTo(savage, wall);
		scn.SkipToPhase(Phase.MANEUVER);

		int twilight = scn.GetTwilight();
		assertTrue(scn.FreepsPlayAvailable(shut));
		scn.FreepsPlayCard(shut);

		// Nothing is discarded. The exertion is a cost that can be paid, so it is offered all the same.
		assertEquals(twilight + 1, scn.GetTwilight());
		assertInZone(Zone.SUPPORT, ships);
		assertAttachedTo(wall, savage);
		assertTrue(scn.FreepsDecisionAvailable("exert a Wraith"));
		scn.FreepsChooseNo();

		assertEquals(0, scn.GetWoundsOn(deadman1));
		assertEquals(0, scn.GetWoundsOn(deadman2));
		assertEquals(0, scn.GetWoundsOn(deadman3));
		assertFalse(scn.IsHindered(ships));
		assertFalse(scn.IsHindered(wall));
		assertInDiscard(shut);
		assertTrue(scn.AwaitingShadowManeuverPhaseActions());
	}

	@Test
	public void AShadowMinionWhoIsAWraithMayBeTheWraithExerted() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var shut = scn.GetFreepsCard("shut1");
		var deadman1 = scn.GetFreepsCard("deadman1");
		var deadman2 = scn.GetFreepsCard("deadman2");
		var deadman3 = scn.GetFreepsCard("deadman3");
		var pinned = scn.GetShadowCard("pinned");
		var burden = scn.GetShadowCard("burden");
		var corpse = scn.GetShadowCard("corpse");
		var savage = scn.GetShadowCard("savage");
		scn.MoveCompanionsToTable(deadman1, deadman2, deadman3);
		scn.MoveCardsToHand(shut);
		scn.MoveCardsToSupportArea(pinned, burden);

		scn.StartGame();
		scn.MoveMinionsToTable(corpse, savage);
		scn.SkipToPhase(Phase.MANEUVER);

		scn.FreepsPlayCard(shut);
		scn.FreepsChooseCard(pinned);
		scn.FreepsChooseYes();

		// "A Wraith", where the skirmish half says "your Wraith": the Shadow player's Wraith is one too, and the Uruk-hai is not.
		assertTrue(scn.FreepsHasCardChoiceAvailable(deadman1, deadman2, deadman3, corpse));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(savage));
		assertEquals(0, scn.GetWoundsOn(corpse));
		scn.FreepsChooseCard(corpse);

		assertEquals(1, scn.GetWoundsOn(corpse));
		assertEquals(0, scn.GetWoundsOn(deadman1));
		assertEquals(0, scn.GetWoundsOn(deadman2));
		assertEquals(0, scn.GetWoundsOn(deadman3));
		// The one Shadow condition left is hindered without asking which.
		assertTrue(scn.IsHindered(burden));
		assertTrue(scn.AwaitingShadowManeuverPhaseActions());
	}

	@Test
	public void AConditionWhoseDiscardWasPreventedIsNotTheOtherConditionHindered() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var shut = scn.GetFreepsCard("shut1");
		var deadman1 = scn.GetFreepsCard("deadman1");
		var deadman2 = scn.GetFreepsCard("deadman2");
		var deadman3 = scn.GetFreepsCard("deadman3");
		var pinned = scn.GetShadowCard("pinned");
		var burden = scn.GetShadowCard("burden");
		var deceit = scn.GetShadowCard("deceit");
		var savage = scn.GetShadowCard("savage");
		scn.MoveCompanionsToTable(deadman1, deadman2, deadman3);
		scn.MoveCardsToHand(shut);
		// Three Shadow conditions. Deceit can save Heavy Burden, the other [gollum] one, from the discard.
		scn.MoveCardsToSupportArea(pinned, burden, deceit);

		scn.StartGame();
		scn.MoveMinionsToTable(savage);
		scn.SkipToPhase(Phase.MANEUVER);

		int twilight = scn.GetTwilight();
		scn.FreepsPlayCard(shut);
		assertTrue(scn.FreepsHasCardChoiceAvailable(pinned, burden, deceit));
		scn.FreepsChooseCard(burden);

		assertTrue(scn.ShadowHasOptionalTriggerAvailable(deceit));
		scn.ShadowChooseOptionalTrigger(deceit);
		// + 1 for the event - 1 removed by Deceit
		assertEquals(twilight, scn.GetTwilight());
		assertInZone(Zone.SUPPORT, burden);

		// The discard was prevented, and the exertion is offered all the same.
		assertTrue(scn.FreepsDecisionAvailable("exert a Wraith"));
		scn.FreepsChooseYes();
		assertEquals(0, scn.GetWoundsOn(deadman1));
		scn.FreepsChooseCard(deadman1);
		assertEquals(1, scn.GetWoundsOn(deadman1));

		// Another Shadow condition: either of the two not chosen for the discard, and not the one that was, though it is still in play.
		assertTrue(scn.FreepsHasCardChoiceAvailable(pinned, deceit));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(burden));
		assertEquals(1, scn.FreepsGetChoiceMin());
		scn.FreepsChooseCard(deceit);

		assertTrue(scn.IsHindered(deceit));
		assertFalse(scn.IsHindered(burden));
		assertFalse(scn.IsHindered(pinned));
		assertInZone(Zone.SUPPORT, burden);
		assertInDiscard(shut);
		assertTrue(scn.AwaitingShadowManeuverPhaseActions());
	}

	@Test
	public void SkirmishMakesOneOfYourWraithsStrengthPlusOneForEachThreat() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var shut = scn.GetFreepsCard("shut1");
		var deadman1 = scn.GetFreepsCard("deadman1");
		var deadman2 = scn.GetFreepsCard("deadman2");
		var gimli = scn.GetFreepsCard("gimli");
		var knight = scn.GetFreepsCard("knight");
		var frodo = scn.GetRingBearer();
		var pinned = scn.GetShadowCard("pinned");
		var savage = scn.GetShadowCard("savage");
		var corpse = scn.GetShadowCard("corpse");
		// Only two [gondor] Wraiths: the spot of 3 belongs to the maneuver half.
		scn.MoveCompanionsToTable(deadman1, deadman2, gimli, knight);
		scn.ApplyAdHocModifier(new IsAdditionalRaceModifier(null, gimli, null, Race.WRAITH));
		scn.MoveCardsToHand(shut);
		scn.MoveCardsToSupportArea(pinned);

		scn.StartGame();
		scn.MoveMinionsToTable(savage, corpse);
		scn.AddThreats(2);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(
				new PhysicalCardImpl[] { deadman1, savage },
				new PhysicalCardImpl[] { knight, corpse }
		);
		scn.FreepsResolveSkirmish(deadman1);

		int twilight = scn.GetTwilight();
		assertEquals(2, scn.GetThreats());
		assertEquals(6, scn.GetStrength(deadman1));
		assertTrue(scn.FreepsPlayAvailable(shut));
		scn.FreepsPlayCard(shut);

		// Your Wraith: the one in the skirmish, one that is not, and the [dwarven] one. Not the Man, and not the Shadow player's Wraith.
		assertTrue(scn.FreepsHasCardChoiceAvailable(deadman1, deadman2, gimli));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(knight, corpse, frodo, savage));
		assertEquals(1, scn.FreepsGetChoiceMin());
		scn.FreepsChooseCard(deadman1);

		// 6 + 2 threats
		assertEquals(8, scn.GetStrength(deadman1));
		assertEquals(6, scn.GetStrength(deadman2));
		assertEquals(6, scn.GetStrength(gimli));
		assertEquals(twilight + 1, scn.GetTwilight());
		assertInDiscard(shut);
		// The maneuver half is not carried out in a skirmish.
		assertInZone(Zone.SUPPORT, pinned);
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());
	}

	@Test
	public void TheBonusIsCappedAtThreeAndASecondCopyHasACapOfItsOwn() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var shut1 = scn.GetFreepsCard("shut1");
		var shut2 = scn.GetFreepsCard("shut2");
		var deadman1 = scn.GetFreepsCard("deadman1");
		var deadman2 = scn.GetFreepsCard("deadman2");
		var gimli = scn.GetFreepsCard("gimli");
		var knight = scn.GetFreepsCard("knight");
		var savage = scn.GetShadowCard("savage");
		// Five companions with Frodo, so that four threats can be on the dead pile.
		scn.MoveCompanionsToTable(deadman1, deadman2, gimli, knight);
		scn.MoveCardsToHand(shut1, shut2);

		scn.StartGame();
		scn.MoveMinionsToTable(savage);
		scn.AddThreats(4);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(deadman1, savage);
		scn.FreepsResolveSkirmish(deadman1);

		assertEquals(4, scn.GetThreats());
		assertEquals(6, scn.GetStrength(deadman1));
		scn.FreepsPlayCard(shut1);
		scn.FreepsChooseCard(deadman1);
		// 6 + 3, the limit, with 4 threats to spot
		assertEquals(9, scn.GetStrength(deadman1));

		scn.ShadowPass();
		assertTrue(scn.FreepsPlayAvailable(shut2));
		scn.FreepsPlayCard(shut2);
		scn.FreepsChooseCard(deadman1);
		// The limit is each copy's own: 9 + 3
		assertEquals(12, scn.GetStrength(deadman1));
		assertEquals(6, scn.GetStrength(deadman2));
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());
	}

	@Test
	public void TheBonusLastsUntilTheEndOfThatSkirmish() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var shut = scn.GetFreepsCard("shut1");
		var deadman1 = scn.GetFreepsCard("deadman1");
		var knight = scn.GetFreepsCard("knight");
		var savage = scn.GetShadowCard("savage");
		var runner = scn.GetShadowCard("runner");
		scn.MoveCompanionsToTable(deadman1, knight);
		scn.MoveCardsToHand(shut);

		scn.StartGame();
		scn.MoveMinionsToTable(savage, runner);
		scn.AddThreats(1);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(
				new PhysicalCardImpl[] { deadman1, savage },
				new PhysicalCardImpl[] { knight, runner }
		);
		scn.FreepsResolveSkirmish(deadman1);

		assertEquals(6, scn.GetStrength(deadman1));
		scn.FreepsPlayCard(shut);
		// The only Wraith is taken without asking: 6 + 1 threat
		assertEquals(7, scn.GetStrength(deadman1));

		scn.ShadowPass();
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
		assertEquals(7, scn.GetStrength(deadman1));

		// 7 against the Savage's 5: the Dead Man wins unhurt and the Savage lives.
		scn.FreepsPass();
		assertEquals(1, scn.GetWoundsOn(savage));
		assertEquals(0, scn.GetWoundsOn(deadman1));
		assertTrue(scn.FreepsDecisionAvailable("Choose next skirmish to resolve"));
		assertEquals(6, scn.GetStrength(deadman1));

		scn.FreepsResolveSkirmish(knight);
		assertEquals(6, scn.GetStrength(deadman1));
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
	}

	@Test
	public void WithNoThreatToSpotItCanStillBePlayedAndAddsNothing() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var shut = scn.GetFreepsCard("shut1");
		var deadman1 = scn.GetFreepsCard("deadman1");
		var savage = scn.GetShadowCard("savage");
		scn.MoveCompanionsToTable(deadman1);
		scn.MoveCardsToHand(shut);

		scn.StartGame();
		scn.MoveMinionsToTable(savage);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(deadman1, savage);
		scn.FreepsResolveSkirmish(deadman1);

		int twilight = scn.GetTwilight();
		assertEquals(0, scn.GetThreats());
		assertEquals(6, scn.GetStrength(deadman1));
		assertTrue(scn.FreepsPlayAvailable(shut));
		scn.FreepsPlayCard(shut);

		assertEquals(6, scn.GetStrength(deadman1));
		assertEquals(twilight + 1, scn.GetTwilight());
		assertInDiscard(shut);
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());
	}

	@Test
	public void WithNoWraithOfYoursItCanStillBePlayedInASkirmishAndDoesNothing() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var shut = scn.GetFreepsCard("shut1");
		var gimli = scn.GetFreepsCard("gimli");
		var corpse = scn.GetShadowCard("corpse");
		// No [gondor] card in play, and the only Wraith on the table is the Shadow player's minion.
		scn.MoveCompanionsToTable(gimli);
		scn.MoveCardsToHand(shut);

		scn.StartGame();
		scn.MoveMinionsToTable(corpse);
		scn.AddThreats(2);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(gimli, corpse);
		scn.FreepsResolveSkirmish(gimli);

		int twilight = scn.GetTwilight();
		assertEquals(2, scn.GetThreats());
		assertEquals(6, scn.GetStrength(gimli));
		assertEquals(7, scn.GetStrength(corpse));
		assertTrue(scn.FreepsPlayAvailable(shut));
		scn.FreepsPlayCard(shut);

		// Nobody is asked anything and nobody is stronger: the 2 threats would have made a Wraith 9.
		assertEquals(6, scn.GetStrength(gimli));
		assertEquals(7, scn.GetStrength(corpse));
		assertEquals(twilight + 1, scn.GetTwilight());
		assertInDiscard(shut);
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());
	}
}

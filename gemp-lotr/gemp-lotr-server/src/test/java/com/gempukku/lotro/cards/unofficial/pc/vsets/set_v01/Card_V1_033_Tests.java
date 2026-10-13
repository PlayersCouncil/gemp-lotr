package com.gempukku.lotro.cards.unofficial.pc.vsets.set_v01;

import com.gempukku.lotro.common.*;
import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import com.gempukku.lotro.logic.modifiers.VitalityModifier;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_V1_033_Tests
{

	// The usual Fellowship sites.  Site 2, Ettenmoors, is plains and not a marsh.
	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("thrashing", "101_33");
					// Goblin Patrol Troop: a [moria] Orc with no text.
					put("troop", "1_177");
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	// The same path with Midgewater Marshes as site 2, so that the first Shadow phase is at a marsh and a second
	// move in the regroup phase leaves it for Rivendell Valley, which is not one.
	protected VirtualTableScenario GetMarshScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("thrashing", "101_33");
					put("thrashing2", "101_33");

					// Goblin Patrol Troop: a [moria] Orc with no text.
					put("troop", "1_177");
					// Goblin Runner: a [moria] Orc.  "When you play this minion, you may add (2)"; it is never played.
					put("runner", "1_178");
					// Goblin Spear: "Bearer must be a [moria] minion."  Its other text is about killing a companion.
					put("spear", "1_182");

					// Aragorn, Ranger of the North: strength 8, vitality 4, no damage bonus.
					put("aragorn", "1_89");
					// Gimli, Son of Gloin: strength 6, the tentacle's own.  His skirmish ability is never used.
					put("gimli", "1_13");
				}},
				new HashMap<>()
				{{
					put("site1", "1_319");
					// Midgewater Marshes: "Marsh. Each time a Hobbit moves to Midgewater Marshes, that Hobbit must
					// exert."  Frodo arrives with a wound, which nothing here turns on.
					put("site2", "1_332");
					put("site3", "1_341");
					put("site4", "1_343");
					put("site5", "1_349");
					put("site6", "1_351");
					put("site7", "1_353");
					put("site8", "1_356");
					put("site9", "1_360");
				}},
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void ThrashingTentacleStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: V1
		 * Name: Thrashing Tentacle
		 * Unique: False
		 * Side: Shadow
		 * Culture: Moria
		 * Twilight Cost: 2
		 * Type: Minion
		 * Subtype: Creature
		 * Strength: 6
		 * Vitality: 2
		 * Site Number: 4
		 * Game Text: Tentacle. Fierce. This minion may not bear possessions and is discarded if not at a marsh.
		 * 	Each time this minion loses a skirmish, add a burden (or 2 burdens if you cannot spot a burden).
		 */

		var scn = GetScenario();

		var card = scn.GetFreepsCard("thrashing");

		assertEquals("Thrashing Tentacle", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, card.getBlueprint().getSide());
		assertEquals(Culture.MORIA, card.getBlueprint().getCulture());
		assertEquals(CardType.MINION, card.getBlueprint().getCardType());
		assertEquals(Race.CREATURE, card.getBlueprint().getRace());
		assertTrue(scn.HasKeyword(card, Keyword.TENTACLE));
		assertTrue(scn.HasKeyword(card, Keyword.FIERCE));
		assertEquals(2, card.getBlueprint().getTwilightCost());
		assertEquals(6, card.getBlueprint().getStrength());
		assertEquals(2, card.getBlueprint().getVitality());
		assertEquals(4, card.getBlueprint().getSiteNumber());
	}

	@Test
	public void PlaysFromHandAtAMarshForTwoTwilightAndTwoMoreForRoamingAndStaysInPlay() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetMarshScenario();

		var thrashing = scn.GetShadowCard("thrashing");
		scn.MoveCardsToHand(thrashing);

		scn.StartGame();
		scn.SetTwilight(3);
		scn.FreepsPass();

		assertTrue(scn.HasKeyword(scn.GetCurrentSite(), Keyword.MARSH));
		// 3 set + 1 companion + 1 for Midgewater Marshes
		assertEquals(5, scn.GetTwilight());
		assertInZone(Zone.HAND, thrashing);
		assertTrue(scn.ShadowPlayAvailable(thrashing));

		scn.ShadowPlayCard(thrashing);

		// 2 to play + 2 roaming: its site number is 4 and this is site 2
		assertEquals(1, scn.GetTwilight());
		assertInZone(Zone.SHADOW_CHARACTERS, thrashing);
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void IsDiscardedAsSoonAsItIsPlayedAtASiteThatIsNotAMarsh() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var thrashing = scn.GetShadowCard("thrashing");
		var troop = scn.GetShadowCard("troop");
		scn.MoveCardsToHand(thrashing);

		scn.StartGame();
		scn.MoveMinionsToTable(troop);
		scn.SetTwilight(3);
		scn.FreepsPass();

		assertFalse(scn.HasKeyword(scn.GetCurrentSite(), Keyword.MARSH));
		// 3 set + 1 companion + 2 for Ettenmoors
		assertEquals(6, scn.GetTwilight());
		assertEquals(0, scn.GetShadowDiscardCount());

		scn.ShadowPlayCard(thrashing);

		// The cost is paid all the same: 2 to play + 2 roaming.
		assertEquals(2, scn.GetTwilight());
		assertInZone(Zone.DISCARD, thrashing);
		assertEquals(1, scn.GetShadowDiscardCount());
		// The Orc beside it is no more at a marsh than it was.
		assertInZone(Zone.SHADOW_CHARACTERS, troop);
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void IsDiscardedWhenTheFellowshipMovesOnFromAMarshToASiteThatIsNotOne() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetMarshScenario();

		var thrashing = scn.GetShadowCard("thrashing");
		var troop = scn.GetShadowCard("troop");

		scn.StartGame();
		scn.FreepsPass();
		scn.MoveMinionsToTable(thrashing, troop);

		assertTrue(scn.HasKeyword(scn.GetCurrentSite(), Keyword.MARSH));
		assertInZone(Zone.SHADOW_CHARACTERS, thrashing, troop);

		// Nobody is assigned, so both minions are still there when the Free Peoples player decides whether to move.
		scn.SkipToMovementDecision();
		assertEquals(2, scn.GetCurrentSiteNumber());
		assertInZone(Zone.SHADOW_CHARACTERS, thrashing, troop);

		scn.FreepsChooseToMove();

		assertEquals(3, scn.GetCurrentSiteNumber());
		assertFalse(scn.HasKeyword(scn.GetCurrentSite(), Keyword.MARSH));
		assertInZone(Zone.DISCARD, thrashing);
		assertInZone(Zone.SHADOW_CHARACTERS, troop);
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void IsNotOfferedAsBearerOfAPossessionThatOtherMoriaMinionsMayBear() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetMarshScenario();

		var thrashing = scn.GetShadowCard("thrashing");
		var troop = scn.GetShadowCard("troop");
		var runner = scn.GetShadowCard("runner");
		var spear = scn.GetShadowCard("spear");
		scn.MoveCardsToHand(thrashing, spear);

		scn.StartGame();
		scn.MoveMinionsToTable(troop, runner);
		scn.SetTwilight(3);
		scn.FreepsPass();

		// The tentacle would be discarded at Bag End, so it is played here, at the marsh.
		scn.ShadowPlayCard(thrashing);
		assertInZone(Zone.SHADOW_CHARACTERS, thrashing, troop, runner);
		assertTrue(scn.ShadowPlayAvailable(spear));

		scn.ShadowPlayCard(spear);

		// All three are [moria] minions, which is all the Spear asks of its bearer.
		assertTrue(scn.ShadowHasCardChoiceAvailable(troop, runner));
		assertTrue(scn.ShadowHasCardChoiceNotAvailable(thrashing));

		scn.ShadowChooseCard(troop);

		assertAttachedTo(spear, troop);
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void EachLostSkirmishAddsTwoBurdensWithNoBurdenToSpotAndOneOtherwise() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetMarshScenario();

		var thrashing = scn.GetShadowCard("thrashing");
		var thrashing2 = scn.GetShadowCard("thrashing2");
		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCompanionsToTable(aragorn);

		scn.StartGame();
		scn.FreepsPass();
		scn.MoveMinionsToTable(thrashing, thrashing2);
		// Two lost skirmishes are two wounds, which is all the vitality it has.  One more keeps it alive to be read
		// after the second.
		scn.ApplyAdHocModifier(new VitalityModifier(null, thrashing, null, 1));

		scn.SkipToAssignments();
		assertEquals(0, scn.GetBurdens());
		assertEquals(0, scn.GetWoundsOn(thrashing));

		// Aragorn's 8 against 6.  The second copy fights nobody.
		scn.FreepsAssignAndResolve(aragorn, thrashing);
		scn.PassSkirmishActions();
		// The wound for losing and the tentacle's burdens are both required, and the Free Peoples player says
		// which comes first: here the wound.
		scn.FreepsResolveRuleFirst();

		assertEquals(1, scn.GetWoundsOn(thrashing));
		assertEquals(0, scn.GetWoundsOn(aragorn));
		// No burden could be spotted, so 2; and only from the copy that lost.
		assertEquals(2, scn.GetBurdens());

		// It is fierce, so it is fought again.
		scn.PassFierceAssignmentActions();
		scn.FreepsAssignAndResolve(aragorn, thrashing);
		scn.PassFierceSkirmishActions();
		// This time the burden before the wound.
		scn.FreepsResolveActionOrder("Thrashing Tentacle");

		assertEquals(2, scn.GetWoundsOn(thrashing));
		// Now there are burdens to spot, so 1.
		assertEquals(3, scn.GetBurdens());
		assertInZone(Zone.SHADOW_CHARACTERS, thrashing, thrashing2);
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void WithExactlyOneBurdenToSpotALostSkirmishAddsOne() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetMarshScenario();

		var thrashing = scn.GetShadowCard("thrashing");
		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCompanionsToTable(aragorn);

		scn.StartGame();
		scn.AddBurdens(1);
		scn.FreepsPass();
		scn.MoveMinionsToTable(thrashing);

		scn.SkipToAssignments();
		assertEquals(1, scn.GetBurdens());
		assertEquals(0, scn.GetWoundsOn(thrashing));

		scn.FreepsAssignAndResolve(aragorn, thrashing);
		scn.PassSkirmishActions();
		scn.FreepsResolveRuleFirst();

		assertEquals(1, scn.GetWoundsOn(thrashing));
		assertEquals(2, scn.GetBurdens());
		// It survived and is fierce, so the fierce assignment phase is next.
		assertInZone(Zone.SHADOW_CHARACTERS, thrashing);
		assertTrue(scn.AwaitingFreepsAssignmentPhaseActions());
	}

	@Test
	public void ATiedSkirmishIsWonByTheTentacleAndAddsNoBurden() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetMarshScenario();

		var thrashing = scn.GetShadowCard("thrashing");
		var gimli = scn.GetFreepsCard("gimli");
		scn.MoveCompanionsToTable(gimli);

		scn.StartGame();
		scn.FreepsPass();
		scn.MoveMinionsToTable(thrashing);

		scn.SkipToAssignments();
		assertEquals(0, scn.GetBurdens());
		assertEquals(6, scn.GetStrength(gimli));
		assertEquals(6, scn.GetStrength(thrashing));

		// 6 against 6: a tie goes to the Shadow side.
		scn.FreepsAssignAndResolve(gimli, thrashing);
		scn.PassSkirmishActions();

		assertEquals(1, scn.GetWoundsOn(gimli));
		assertEquals(0, scn.GetWoundsOn(thrashing));
		assertEquals(0, scn.GetBurdens());
		assertTrue(scn.AwaitingFreepsAssignmentPhaseActions());
	}

	@Test
	public void LosingTheSkirmishThatKillsItStillAddsABurden() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetMarshScenario();

		var thrashing = scn.GetShadowCard("thrashing");
		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCompanionsToTable(aragorn);

		scn.StartGame();
		scn.AddBurdens(1);
		scn.FreepsPass();
		scn.MoveMinionsToTable(thrashing);
		// One wound already, as it has in the fierce skirmish that follows a lost one.
		scn.AddWoundsToChar(thrashing, 1);

		scn.SkipToAssignments();
		assertEquals(1, scn.GetBurdens());
		assertInZone(Zone.SHADOW_CHARACTERS, thrashing);

		scn.FreepsAssignAndResolve(aragorn, thrashing);
		scn.PassSkirmishActions();
		// The Free Peoples player has the wound placed first, so the tentacle is dead before its burden is added.
		scn.FreepsResolveRuleFirst();

		assertInZone(Zone.DISCARD, thrashing);
		assertEquals(2, scn.GetBurdens());
		// No minion is left, so there is no fierce skirmish.
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}
}

package com.gempukku.lotro.cards.official.set17;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;
import com.gempukku.lotro.game.PhysicalCardImpl;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_17_119_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("aggressor", "17_119");
					put("guard", "10_81");      // Cirith Ungol Guard: a [sauron] Uruk-hai, vitality 2, damage +1 and nothing else
					put("party", "1_158");      // Uruk-hai Raiding Party: an [isengard] Uruk-hai, vitality 3, damage +1 and nothing else
					put("gimli", "1_13");       // Gimli, Son of Gloin: a Dwarf companion who pays Baruk Khazad's exertion
					// Baruk Khazad: [dwarven] Maneuver event, "Exert a Dwarf to wound a minion. You may take the next
					// maneuver action."
					put("baruk", "5_5");
					put("gandalf", "1_72");     // Gandalf, Friend of the Shirefolk: the Gandalf Long I Fell spots
					// Long I Fell: [gandalf] Skirmish event, "Spot Gandalf to prevent all wounds to him. Any Shadow
					// player may make you wound a minion to prevent this."
					put("fell", "4_97");
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void WhiteHandAggressorStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 17
		 * Name: White Hand Aggressor
		 * Unique: False
		 * Side: Shadow
		 * Culture: Uruk-hai
		 * Twilight Cost: 5
		 * Type: Minion
		 * Subtype: Uruk-hai
		 * Strength: 13
		 * Vitality: 1
		 * Site Number: 5
		 * Game Text: <b>Damage +1</b>.<br>While you control a site, this minion cannot take wounds, except during the skirmish phase.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("aggressor");

		assertEquals("White Hand Aggressor", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, card.getBlueprint().getSide());
		assertEquals(Culture.URUK_HAI, card.getBlueprint().getCulture());
		assertEquals(CardType.MINION, card.getBlueprint().getCardType());
		assertEquals(Race.URUK_HAI, card.getBlueprint().getRace());
		assertTrue(scn.HasKeyword(card, Keyword.DAMAGE));
		assertEquals(1, scn.GetKeywordCount(card, Keyword.DAMAGE));
		assertEquals(5, card.getBlueprint().getTwilightCost());
		assertEquals(13, card.getBlueprint().getStrength());
		assertEquals(1, card.getBlueprint().getVitality());
		assertEquals(5, card.getBlueprint().getSiteNumber());
	}

	@Test
	public void WhileItsOwnerControlsASiteItCannotBeWoundedInTheManeuverPhase() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var gimli = scn.GetFreepsCard("gimli");
		var baruk = scn.GetFreepsCard("baruk");
		var aggressor = scn.GetShadowCard("aggressor");
		var guard = scn.GetShadowCard("guard");
		var party = scn.GetShadowCard("party");
		scn.MoveCompanionsToTable(gimli);
		scn.MoveCardsToHand(baruk);

		scn.StartGame();
		// Player 1's third turn: the Shadow player's marker is on site 3 as well, so site 1 can be his.
		scn.SkipToSite(3);
		scn.ShadowTakeControlOfSite();
		assertTrue(scn.ShadowControls(scn.GetSite(1)));
		scn.MoveMinionsToTable(aggressor, guard, party);
		scn.SkipToPhase(Phase.MANEUVER);
		assertInHand(baruk);
		assertEquals(0, scn.GetWoundsOn(aggressor));
		assertEquals(0, scn.GetWoundsOn(guard));
		assertEquals(0, scn.GetWoundsOn(party));

		scn.FreepsPlayCard(baruk);
		assertEquals(1, scn.GetWoundsOn(gimli));
		// A minion that cannot take wounds is not one a wound can be placed on (CR 4.2 "wound"); the other two
		// minions are.
		assertTrue(scn.FreepsHasCardChoiceAvailable(guard, party));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(aggressor));
		scn.FreepsChooseCard(guard);
		assertEquals(1, scn.GetWoundsOn(guard));
		assertEquals(0, scn.GetWoundsOn(aggressor));
		assertInZone(Zone.SHADOW_CHARACTERS, aggressor);
		assertTrue(scn.FreepsDecisionAvailable("Play Maneuver action or Pass"));
	}

	@Test
	public void WithNoSiteControlledItIsWoundedInTheManeuverPhase() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var gimli = scn.GetFreepsCard("gimli");
		var baruk = scn.GetFreepsCard("baruk");
		var aggressor = scn.GetShadowCard("aggressor");
		var guard = scn.GetShadowCard("guard");
		scn.MoveCompanionsToTable(gimli);
		scn.MoveCardsToHand(baruk);

		scn.StartGame();
		scn.SkipToSite(3);
		scn.MoveMinionsToTable(aggressor, guard);
		scn.SkipToPhase(Phase.MANEUVER);
		assertFalse(scn.ShadowControls(scn.GetSite(1)));
		assertInHand(baruk);
		assertEquals(0, scn.GetWoundsOn(aggressor));

		scn.FreepsPlayCard(baruk);
		assertTrue(scn.FreepsHasCardChoiceAvailable(aggressor, guard));
		scn.FreepsChooseCard(aggressor);
		// Vitality 1: the wound kills it.
		assertInDiscard(aggressor);
		assertEquals(0, scn.GetWoundsOn(guard));
		assertTrue(scn.FreepsDecisionAvailable("Play Maneuver action or Pass"));
	}

	@Test
	public void ASiteTheFreePeoplesPlayerControlsDoesNotStopTheWound() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var gimli = scn.GetFreepsCard("gimli");
		var baruk = scn.GetFreepsCard("baruk");
		var aggressor = scn.GetShadowCard("aggressor");
		var guard = scn.GetShadowCard("guard");
		scn.MoveCompanionsToTable(gimli);
		scn.MoveCardsToHand(baruk);

		scn.StartGame();
		scn.SkipToSite(3);
		// Site 1 is the Free Peoples player's, as if taken on one of the other player's turns; the Shadow player
		// controls none.
		scn.FreepsTakeControlOfSite();
		assertTrue(scn.FreepsControls(scn.GetSite(1)));
		scn.MoveMinionsToTable(aggressor, guard);
		scn.SkipToPhase(Phase.MANEUVER);
		assertEquals(0, scn.GetShadowControlledSiteCount());
		assertInHand(baruk);
		assertEquals(0, scn.GetWoundsOn(aggressor));

		scn.FreepsPlayCard(baruk);
		assertTrue(scn.FreepsHasCardChoiceAvailable(aggressor, guard));
		scn.FreepsChooseCard(aggressor);
		assertInDiscard(aggressor);
		assertEquals(0, scn.GetWoundsOn(guard));
		assertTrue(scn.FreepsDecisionAvailable("Play Maneuver action or Pass"));
	}

	@Test
	public void DuringTheSkirmishPhaseItIsWoundedThoughItIsInNoSkirmishOfItsOwn() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var frodo = scn.GetRingBearer();
		var gandalf = scn.GetFreepsCard("gandalf");
		var fell = scn.GetFreepsCard("fell");
		var aggressor = scn.GetShadowCard("aggressor");
		var guard = scn.GetShadowCard("guard");
		scn.MoveCompanionsToTable(gandalf);
		scn.MoveCardsToHand(fell);
		assertInHand(fell);

		scn.StartGame();
		scn.SkipToSite(3);
		scn.ShadowTakeControlOfSite();
		assertTrue(scn.ShadowControls(scn.GetSite(1)));
		scn.MoveMinionsToTable(aggressor, guard);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(new PhysicalCardImpl[]{gandalf, guard}, new PhysicalCardImpl[]{frodo, aggressor});
		// Gandalf's skirmish first; the Aggressor waits for Frodo's.
		scn.FreepsResolveSkirmish(gandalf);
		assertInHand(fell);
		assertEquals(0, scn.GetWoundsOn(aggressor));

		scn.FreepsPlayCard(fell);
		scn.ShadowChooseYes();
		assertTrue(scn.FreepsHasCardChoiceAvailable(aggressor, guard));
		scn.FreepsChooseCard(aggressor);
		// The site is still the Shadow player's, and it is the skirmish phase: the wound is taken and kills it.
		assertTrue(scn.ShadowControls(scn.GetSite(1)));
		assertInDiscard(aggressor);
		assertEquals(0, scn.GetWoundsOn(guard));
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());
	}
}

package com.gempukku.lotro.cards.unofficial.hobbit.set30;

import com.gempukku.lotro.common.*;
import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.game.PhysicalCardImpl;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_30_032_Tests
{
	// The Hobbit table: the Hobbit path and Bilbo, Reliable Companion 30_46 as the Ring-bearer; the skirmishes of
	// turn 1 are at The Troll Hoard (site 2), whose only text is a start-of-turn offer.
	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("anger1", "30_32");
					put("anger2", "30_32");
					put("anger3", "30_32");

					put("nori", "30_15");       // Nori, Of the Blue Mountains: Dwarf companion; a maneuver ability not used
					put("dwalin", "30_9");      // Dwalin, Brother of Balin: Dwarf companion; damage +1 and a maneuver ability
					// Followers of two cultures.  Bifur's text answers a fierce win; Gwaihir's skirmish ability cancels a
					// skirmish against a mounted Orc, and is not used.
					put("bifur", "30_5");       // Bifur, Inarticulate: a [dwarven] follower
					put("gwaihir", "31_15");    // Gwaihir, Lord of the Eagles: a [gandalf] follower
					put("key", "32_11");        // Thrór's Key: a [dwarven] possession for a [dwarven] companion, no follower;
					                            // its texts are for moves from site 6 or 7 and the regroup phase

					put("aggressor", "30_39");  // Orkish Aggressor: [gundabad] Orc, strength 6; an assignment ability not used
					put("bats", "32_30");       // Dark Bats: [moria] creature, strength 4; a Shadow ability not used
					put("veteran", "32_43");    // Orkish Veteran: [sauron] Orc, strength 7; texts for site 9 and the regroup phase
				}},
				null, null, null,
				VirtualTableScenario.HobbitDraft
		);
	}

	@Test
	public void AngerStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 30
		 * Name: Anger
		 * Unique: False
		 * Side: Shadow
		 * Culture: Gundabad
		 * Twilight Cost: 1
		 * Type: Event
		 * Subtype: Skirmish
		 * Game Text: Make a minion strength +2 (or +4 if skirmishing a character bearing a follower).
		 */

		var scn = GetScenario();

		var card = scn.GetShadowCard("anger1");

		assertEquals("Anger", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, card.getBlueprint().getSide());
		assertEquals(Culture.GUNDABAD, card.getBlueprint().getCulture());
		assertEquals(CardType.EVENT, card.getBlueprint().getCardType());
		assertTrue(scn.HasTimeword(card, Timeword.SKIRMISH));
		assertEquals(1, card.getBlueprint().getTwilightCost());
	}

	@Test
	public void MakesAnyMinionPlusTwoForThatSkirmishOnly() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var anger = scn.GetShadowCard("anger1");
		var nori = scn.GetFreepsCard("nori");
		var dwalin = scn.GetFreepsCard("dwalin");
		var bilbo = scn.GetRingBearer();
		var aggressor = scn.GetShadowCard("aggressor");
		var bats = scn.GetShadowCard("bats");
		var veteran = scn.GetShadowCard("veteran");
		scn.MoveCompanionsToTable(nori, dwalin);
		scn.MoveCardsToHand(anger);

		scn.StartGame();
		assertInZone(Zone.HAND, anger);
		scn.SetTwilight(5);
		scn.MoveMinionsToTable(aggressor, bats, veteran);
		scn.SkipToPhase(Phase.ASSIGNMENT);

		// The phase next door: not among the Shadow player's assignment actions.
		scn.FreepsPass();
		assertTrue(scn.AwaitingShadowAssignmentPhaseActions());
		assertFalse(scn.ShadowPlayAvailable(anger));
		scn.ShadowPass();

		scn.FreepsAssignToMinions(new PhysicalCardImpl[]{nori, aggressor}, new PhysicalCardImpl[]{dwalin, bats});
		// The Orkish Veteran is left over, and stays unassigned.
		scn.ShadowDeclineAssignments();
		scn.FreepsResolveSkirmish(nori);
		scn.FreepsPass();

		assertEquals(6, scn.GetStrength(aggressor));
		assertEquals(4, scn.GetStrength(bats));
		assertEquals(7, scn.GetStrength(veteran));
		assertTrue(scn.ShadowPlayAvailable(anger));

		scn.ShadowPlayCard(anger);
		// The minion in this skirmish, one assigned to the next and one in none, of three cultures and two races.
		// No companion.
		assertTrue(scn.ShadowHasCardChoiceAvailable(aggressor, bats, veteran));
		assertTrue(scn.ShadowHasCardChoiceNotAvailable(nori, dwalin, bilbo));
		assertEquals(1, scn.ShadowGetChoiceMin());
		scn.ShadowChooseCard(aggressor);

		// 6 + 2: Nori bears nothing.
		assertEquals(8, scn.GetStrength(aggressor));
		assertEquals(4, scn.GetStrength(bats));
		assertEquals(7, scn.GetStrength(veteran));
		assertInZone(Zone.DISCARD, anger);
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());

		// Each skirmish is a phase of its own: in Dwalin's skirmish the bonus is gone.
		scn.PassSkirmishActions();
		scn.FreepsResolveSkirmish(dwalin);
		assertEquals(6, scn.GetStrength(aggressor));
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
	}

	@Test
	public void AMinionSkirmishingACompanionBearingAFollowerOfEitherCultureIsPlusFourAndAnotherMinionPlusTwo() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var anger1 = scn.GetShadowCard("anger1");
		var anger2 = scn.GetShadowCard("anger2");
		var anger3 = scn.GetShadowCard("anger3");
		var nori = scn.GetFreepsCard("nori");
		var dwalin = scn.GetFreepsCard("dwalin");
		var bifur = scn.GetFreepsCard("bifur");
		var gwaihir = scn.GetFreepsCard("gwaihir");
		var aggressor = scn.GetShadowCard("aggressor");
		var bats = scn.GetShadowCard("bats");
		var veteran = scn.GetShadowCard("veteran");
		scn.MoveCompanionsToTable(nori, dwalin);
		// Nori bears a [gandalf] follower, Dwalin a [dwarven] one (each could have come there by its Aid).
		scn.AttachCardsTo(nori, gwaihir);
		scn.AttachCardsTo(dwalin, bifur);
		scn.MoveCardsToHand(anger1, anger2, anger3);

		scn.StartGame();
		assertEquals(nori, gwaihir.getAttachedTo());
		assertEquals(dwalin, bifur.getAttachedTo());
		assertInZone(Zone.HAND, anger1, anger2, anger3);
		scn.SetTwilight(5);
		scn.MoveMinionsToTable(aggressor, bats, veteran);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(new PhysicalCardImpl[]{nori, aggressor}, new PhysicalCardImpl[]{dwalin, bats});
		scn.ShadowDeclineAssignments();

		scn.FreepsResolveSkirmish(nori);
		scn.FreepsPass();
		assertEquals(6, scn.GetStrength(aggressor));
		scn.ShadowPlayCard(anger1);
		scn.ShadowChooseCard(aggressor);
		// 6 + 4: skirmishing Nori, who bears Gwaihir.
		assertEquals(10, scn.GetStrength(aggressor));

		scn.PassSkirmishActions();
		scn.FreepsResolveSkirmish(dwalin);
		scn.FreepsPass();
		assertEquals(4, scn.GetStrength(bats));
		assertEquals(7, scn.GetStrength(veteran));

		// The Orkish Veteran skirmishes nobody, while the Dark Bats skirmish Dwalin, who bears Bifur.
		scn.ShadowPlayCard(anger2);
		scn.ShadowChooseCard(veteran);
		// 7 + 2
		assertEquals(9, scn.GetStrength(veteran));
		assertEquals(4, scn.GetStrength(bats));

		scn.FreepsPass();
		scn.ShadowPlayCard(anger3);
		scn.ShadowChooseCard(bats);
		// 4 + 4
		assertEquals(8, scn.GetStrength(bats));
		assertEquals(9, scn.GetStrength(veteran));
		assertInZone(Zone.DISCARD, anger1, anger2, anger3);
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
	}

	@Test
	public void AMinionSkirmishingACompanionBearingAPossessionOrAssignedToAFollowerBearerIsPlusTwo() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var anger1 = scn.GetShadowCard("anger1");
		var anger2 = scn.GetShadowCard("anger2");
		var nori = scn.GetFreepsCard("nori");
		var dwalin = scn.GetFreepsCard("dwalin");
		var bifur = scn.GetFreepsCard("bifur");
		var key = scn.GetFreepsCard("key");
		var aggressor = scn.GetShadowCard("aggressor");
		var bats = scn.GetShadowCard("bats");
		scn.MoveCompanionsToTable(nori, dwalin);
		// Nori bears a card that is no follower; Dwalin, whose skirmish comes second, bears a follower.
		scn.AttachCardsTo(nori, key);
		scn.AttachCardsTo(dwalin, bifur);
		scn.MoveCardsToHand(anger1, anger2);

		scn.StartGame();
		assertEquals(nori, key.getAttachedTo());
		assertEquals(dwalin, bifur.getAttachedTo());
		assertInZone(Zone.HAND, anger1, anger2);
		scn.SetTwilight(5);
		scn.MoveMinionsToTable(aggressor, bats);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(new PhysicalCardImpl[]{nori, aggressor}, new PhysicalCardImpl[]{dwalin, bats});

		scn.FreepsResolveSkirmish(nori);
		scn.FreepsPass();
		assertEquals(6, scn.GetStrength(aggressor));
		assertEquals(4, scn.GetStrength(bats));

		scn.ShadowPlayCard(anger1);
		scn.ShadowChooseCard(aggressor);
		// 6 + 2: Thrór's Key is no follower.
		assertEquals(8, scn.GetStrength(aggressor));

		scn.FreepsPass();
		scn.ShadowPlayCard(anger2);
		scn.ShadowChooseCard(bats);
		// 4 + 2: assigned to Dwalin, the Dark Bats are not yet skirmishing him.
		assertEquals(6, scn.GetStrength(bats));
		assertEquals(8, scn.GetStrength(aggressor));
		assertInZone(Zone.DISCARD, anger1, anger2);
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
	}
}

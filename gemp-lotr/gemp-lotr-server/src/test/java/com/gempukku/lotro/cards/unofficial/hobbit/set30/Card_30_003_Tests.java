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

public class Card_30_003_Tests
{
	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("fury1", "30_3");
					put("fury2", "30_3");

					put("nori", "30_15");       // Nori, Of the Blue Mountains: Dwarf companion, strength 7; a maneuver ability
					put("dwalin", "30_9");      // Dwalin, Brother of Balin: Dwarf companion, strength 7; damage +1 and a
					                            // maneuver ability
					put("dain", "32_2");        // Dain Ironfoot, Lord of the Iron Hills: Dwarf ally, strength 7; his bonus is
					                            // Thorin's, who is not laid
					// [dwarven] followers of two races: Bifur a Dwarf, Roäc a Bird.  Their texts answer a fierce win
					// and the maneuver phase.
					put("bifur", "30_5");
					put("roac", "32_8");
					put("gwaihir", "31_15");    // Gwaihir, Lord of the Eagles: a [gandalf] follower; his skirmish ability
					                            // cancels a skirmish against a mounted Orc, and none is laid
					put("key", "32_11");        // Thrór's Key: a [dwarven] possession for a [dwarven] companion, no follower;
					                            // no strength, and its texts are for moves from site 6 or 7 and the regroup phase

					put("veteran", "32_43");    // Orkish Veteran: [sauron] Orc, strength 7; texts for site 9 and the regroup phase
					put("pursuer", "32_42");    // Orkish Pursuer: [sauron] Orc, strength 8; a win trigger and a regroup ability
				}},
				null, null, null,
				VirtualTableScenario.HobbitDraft
		);
	}

	@Test
	public void BattleFuryStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 30
		 * Name: Battle Fury
		 * Unique: False
		 * Side: Free Peoples
		 * Culture: Dwarven
		 * Twilight Cost: 1
		 * Type: Event
		 * Subtype: Skirmish
		 * Game Text: Make a Dwarf character strength +3 (or +4 if he bears a [dwarven] follower).
		 */

		var scn = GetScenario();

		var card = scn.GetFreepsCard("fury1");

		assertEquals("Battle Fury", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.FREE_PEOPLE, card.getBlueprint().getSide());
		assertEquals(Culture.DWARVEN, card.getBlueprint().getCulture());
		assertEquals(CardType.EVENT, card.getBlueprint().getCardType());
		assertTrue(scn.HasTimeword(card, Timeword.SKIRMISH));
		assertEquals(1, card.getBlueprint().getTwilightCost());
	}

	@Test
	public void MakesADwarfCharacterPlusThreeForThatSkirmishOnly() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var fury = scn.GetFreepsCard("fury1");
		var nori = scn.GetFreepsCard("nori");
		var dwalin = scn.GetFreepsCard("dwalin");
		var dain = scn.GetFreepsCard("dain");
		var bifur = scn.GetFreepsCard("bifur");
		var bilbo = scn.GetRingBearer();
		var veteran = scn.GetShadowCard("veteran");
		var pursuer = scn.GetShadowCard("pursuer");
		scn.MoveCompanionsToTable(nori, dwalin);
		scn.MoveAlliesToTable(dain);
		// A Dwarf who is no character: the HDG's followers carry a race.
		scn.MoveCardsToSupportArea(bifur);
		scn.MoveCardsToHand(fury);

		scn.StartGame();
		scn.MoveMinionsToTable(veteran, pursuer);
		scn.SkipToPhase(Phase.ASSIGNMENT);

		// The phase next door: not among the Free Peoples player's assignment actions.
		assertTrue(scn.AwaitingFreepsAssignmentPhaseActions());
		assertFalse(scn.FreepsPlayAvailable(fury));

		scn.PassCurrentPhaseActions();
		scn.FreepsAssignToMinions(new PhysicalCardImpl[]{nori, veteran}, new PhysicalCardImpl[]{dwalin, pursuer});
		scn.FreepsResolveSkirmish(nori);

		assertEquals(7, scn.GetStrength(nori));
		assertEquals(7, scn.GetStrength(dwalin));
		assertEquals(7, scn.GetStrength(dain));
		assertTrue(scn.FreepsPlayAvailable(fury));

		scn.FreepsPlayCard(fury);
		// Nori in the skirmish, Dwalin assigned to the next one and Dain, an ally in none: the text names no
		// skirmish.  Not Bilbo, no Dwarf, and not Bifur, a Dwarf follower and no character.
		assertTrue(scn.FreepsHasCardChoiceAvailable(nori, dwalin, dain));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(bilbo, bifur));
		assertEquals(1, scn.FreepsGetChoiceMin());
		assertEquals(1, scn.FreepsGetChoiceMax());

		scn.FreepsChooseCard(nori);
		// 7 + 3
		assertEquals(10, scn.GetStrength(nori));
		assertEquals(7, scn.GetStrength(dwalin));
		assertEquals(7, scn.GetStrength(dain));
		assertInZone(Zone.DISCARD, fury);
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());

		// Each skirmish is a phase of its own: in Dwalin's skirmish the bonus is gone.
		scn.PassSkirmishActions();
		scn.FreepsResolveSkirmish(dwalin);
		assertEquals(7, scn.GetStrength(nori));
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
	}

	@Test
	public void ADwarfCharacterBearingADwarvenFollowerOfEitherRaceIsPlusFour() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var fury1 = scn.GetFreepsCard("fury1");
		var fury2 = scn.GetFreepsCard("fury2");
		var nori = scn.GetFreepsCard("nori");
		var dwalin = scn.GetFreepsCard("dwalin");
		var bifur = scn.GetFreepsCard("bifur");
		var roac = scn.GetFreepsCard("roac");
		var veteran = scn.GetShadowCard("veteran");
		var pursuer = scn.GetShadowCard("pursuer");
		scn.MoveCompanionsToTable(nori, dwalin);
		scn.AttachCardsTo(nori, bifur);
		scn.AttachCardsTo(dwalin, roac);
		scn.MoveCardsToHand(fury1, fury2);

		scn.StartGame();
		assertInZone(Zone.ATTACHED, bifur, roac);
		assertEquals(nori, bifur.getAttachedTo());
		assertEquals(dwalin, roac.getAttachedTo());
		scn.MoveMinionsToTable(veteran, pursuer);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(new PhysicalCardImpl[]{nori, veteran}, new PhysicalCardImpl[]{dwalin, pursuer});
		scn.FreepsResolveSkirmish(nori);

		int noriStrength = scn.GetStrength(nori);
		int dwalinStrength = scn.GetStrength(dwalin);

		// Nori bears Bifur, a [dwarven] follower who is a Dwarf.
		scn.FreepsPlayCard(fury1);
		scn.FreepsChooseCard(nori);
		assertEquals(noriStrength + 4, scn.GetStrength(nori));
		assertEquals(dwalinStrength, scn.GetStrength(dwalin));

		scn.PassSkirmishActions();
		scn.FreepsResolveSkirmish(dwalin);

		// Dwalin bears Roäc, a [dwarven] follower who is a Bird.
		scn.FreepsPlayCard(fury2);
		scn.FreepsChooseCard(dwalin);
		assertEquals(dwalinStrength + 4, scn.GetStrength(dwalin));
		assertInZone(Zone.DISCARD, fury1, fury2);
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());
	}

	@Test
	public void ADwarfCharacterBearingAFollowerOfAnotherCultureOrADwarvenPossessionIsPlusThreeBesideOneWhoBearsADwarvenFollower() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var fury = scn.GetFreepsCard("fury1");
		var nori = scn.GetFreepsCard("nori");
		var dwalin = scn.GetFreepsCard("dwalin");
		var bifur = scn.GetFreepsCard("bifur");
		var gwaihir = scn.GetFreepsCard("gwaihir");
		var key = scn.GetFreepsCard("key");
		var veteran = scn.GetShadowCard("veteran");
		scn.MoveCompanionsToTable(nori, dwalin);
		// Nori bears a follower of the [gandalf] culture and a [dwarven] card that is no follower; Dwalin, beside
		// him in no skirmish, bears a [dwarven] follower.
		scn.AttachCardsTo(nori, gwaihir, key);
		scn.AttachCardsTo(dwalin, bifur);
		scn.MoveCardsToHand(fury);

		scn.StartGame();
		assertInZone(Zone.ATTACHED, gwaihir, key, bifur);
		assertEquals(nori, gwaihir.getAttachedTo());
		assertEquals(nori, key.getAttachedTo());
		assertEquals(dwalin, bifur.getAttachedTo());
		scn.MoveMinionsToTable(veteran);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(nori, veteran);
		scn.FreepsResolveSkirmish(nori);

		int noriStrength = scn.GetStrength(nori);
		int dwalinStrength = scn.GetStrength(dwalin);

		scn.FreepsPlayCard(fury);
		scn.FreepsChooseCard(nori);
		assertEquals(noriStrength + 3, scn.GetStrength(nori));
		assertEquals(dwalinStrength, scn.GetStrength(dwalin));
		assertInZone(Zone.DISCARD, fury);
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());
	}
}

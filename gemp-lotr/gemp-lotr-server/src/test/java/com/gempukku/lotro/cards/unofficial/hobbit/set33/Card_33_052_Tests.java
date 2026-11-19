package com.gempukku.lotro.cards.unofficial.hobbit.set33;

import com.gempukku.lotro.common.*;
import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.game.PhysicalCardImpl;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_33_052_Tests
{

	// The Hobbit table: the Hobbit path and Bilbo, Reliable Companion 30_46 (vitality 4) as the Ring-bearer. The
	// skirmishes of turn 1 are at The Troll Hoard (site 2).
	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("pony", "33_52");
					// Thorin, Oakenshield: [dwarven] Dwarf companion, strength 7 (+1 for each [dwarven] follower: none);
					// a fellowship ability only
					put("thorin", "30_19");
					// Dwalin, Brother of Balin: [dwarven] Dwarf companion, strength 7; a maneuver ability only
					put("dwalin", "30_9");
					// Gandalf, Friend of Thorin: [gandalf] Wizard companion, strength 7; a cost modifier on Thorin and
					// a fellowship ability only
					put("gandalf", "30_26");
					// Iron Hills Army: [dwarven] Dwarf ally, strength 9, home 9H; a maneuver ability only
					put("army", "32_5");
					// Master Worrywort, Hobbit Farmer: [shire] Hobbit ally; a trigger on Bilbo's wins and a skirmish
					// ability only
					put("worrywort", "33_53");
					// Pippin, Friend to Frodo: a [shire] Hobbit companion who is no Bilbo, able to exert; a bar on
					// discarding tales only
					put("pippin", "1_306");

					// Goblin Footman: [moria] Orc, strength 8, vitality 1; a play trigger only
					put("footman", "30_42");
					// Dark Bats: [moria] Creature, strength 4, vitality 2; a Shadow ability only
					put("bats", "32_30");
					// Orkish Aggressor: [gundabad] Orc, strength 6, vitality 2; an Assignment ability used only on purpose
					put("aggressor", "30_39");
				}},
				null, null, null,
				VirtualTableScenario.HobbitDraft
		);
	}

	@Test
	public void BilbosPonyStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 33
		 * Name: Bilbo's Pony
		 * Unique: True
		 * Side: Free Peoples
		 * Culture: Shire
		 * Twilight Cost: 1
		 * Type: Possession
		 * Subtype: Mount
		 * Strength: 1
		 * Game Text: Bearer must be Bilbo.<br><b>Skirmish:</b> If Bilbo is assigned to a skirmish, exert him to make a [dwarven] companion strength +2 (limit +2).
		 */

		var scn = GetScenario();

		var card = scn.GetFreepsCard("pony");

		assertEquals("Bilbo's Pony", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertTrue(card.getBlueprint().isUnique());
		assertEquals(Side.FREE_PEOPLE, card.getBlueprint().getSide());
		assertEquals(Culture.SHIRE, card.getBlueprint().getCulture());
		assertEquals(CardType.POSSESSION, card.getBlueprint().getCardType());
		assertTrue(card.getBlueprint().getPossessionClasses().contains(PossessionClass.MOUNT));
		assertEquals(1, card.getBlueprint().getTwilightCost());
		assertEquals(1, card.getBlueprint().getStrength());
	}

	@Test
	public void BilbosPonyIsPlayedOnBilboAndOnNoOtherCharacter() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var pony = scn.GetFreepsCard("pony");
		var bilbo = scn.GetRingBearer();
		var thorin = scn.GetFreepsCard("thorin");
		var pippin = scn.GetFreepsCard("pippin");
		var worrywort = scn.GetFreepsCard("worrywort");
		scn.MoveCompanionsToTable(thorin, pippin);
		scn.MoveAlliesToTable(worrywort);
		scn.MoveCardsToHand(pony);

		scn.StartGame();
		assertInHand(pony);

		assertTrue(scn.FreepsPlayAvailable(pony));
		scn.FreepsPlayCard(pony);
		// Bilbo is its one bearer: no choice is put, though a Dwarf companion, a Hobbit companion who is no Bilbo
		// and a Hobbit ally stand beside him.
		assertAttachedTo(pony, bilbo);
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void SkirmishActionExertsBilboToMakeADwarvenCompanionStrongerUpToPlus2EachSkirmish() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var pony = scn.GetFreepsCard("pony");
		var bilbo = scn.GetRingBearer();
		var thorin = scn.GetFreepsCard("thorin");
		var dwalin = scn.GetFreepsCard("dwalin");
		var gandalf = scn.GetFreepsCard("gandalf");
		var army = scn.GetFreepsCard("army");
		var footman = scn.GetShadowCard("footman");
		var bats = scn.GetShadowCard("bats");
		var aggressor = scn.GetShadowCard("aggressor");

		scn.StartGame();
		scn.MoveCompanionsToTable(thorin, dwalin, gandalf);
		scn.MoveAlliesToTable(army);
		scn.AttachCardsTo(bilbo, pony);
		scn.MoveMinionsToTable(footman, bats, aggressor);
		assertAttachedTo(pony, bilbo);

		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(new PhysicalCardImpl[]{bilbo, bats}, new PhysicalCardImpl[]{thorin, footman},
				new PhysicalCardImpl[]{gandalf, aggressor});
		scn.FreepsResolveSkirmish(thorin);
		assertEquals(0, scn.GetWoundsOn(bilbo));
		assertEquals(7, scn.GetStrength(thorin));
		assertEquals(7, scn.GetStrength(dwalin));

		// Bilbo is assigned to a skirmish still to come.
		assertTrue(scn.FreepsActionAvailable(pony));
		scn.FreepsUseCardAction(pony);
		assertEquals(1, scn.GetWoundsOn(bilbo));
		// Thorin, skirmishing, and Dwalin, in no skirmish; not Gandalf (a [gandalf] companion), the Iron Hills Army
		// (a [dwarven] ally) or Bilbo ([shire]).
		assertTrue(scn.FreepsHasCardChoiceAvailable(thorin, dwalin));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(gandalf, army, bilbo));
		assertEquals(1, scn.FreepsGetChoiceMin());
		scn.FreepsChooseCard(dwalin);
		assertEquals(9, scn.GetStrength(dwalin));
		assertEquals(7, scn.GetStrength(thorin));
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());

		// At the limit: offered again, Bilbo exerts, and Thorin gets nothing.
		scn.ShadowPass();
		assertTrue(scn.FreepsActionAvailable(pony));
		scn.FreepsUseCardAction(pony);
		assertEquals(2, scn.GetWoundsOn(bilbo));
		scn.FreepsChooseCard(thorin);
		assertEquals(7, scn.GetStrength(thorin));
		assertEquals(9, scn.GetStrength(dwalin));

		// Thorin, 7, loses to the Footman, 8. In the next skirmish Dwalin's +2 is gone and the limit is new.
		scn.PassSkirmishActions();
		assertEquals(1, scn.GetWoundsOn(thorin));
		assertTrue(scn.FreepsAwaitingSkirmishChoice());
		scn.FreepsResolveSkirmish(gandalf);
		assertEquals(7, scn.GetStrength(dwalin));

		assertTrue(scn.FreepsActionAvailable(pony));
		scn.FreepsUseCardAction(pony);
		assertEquals(3, scn.GetWoundsOn(bilbo));
		scn.FreepsChooseCard(dwalin);
		assertEquals(9, scn.GetStrength(dwalin));
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());
	}

	@Test
	public void SkirmishActionIsNotOfferedWhileBilboIsNotAssigned() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var pony = scn.GetFreepsCard("pony");
		var bilbo = scn.GetRingBearer();
		var thorin = scn.GetFreepsCard("thorin");
		var footman = scn.GetShadowCard("footman");

		scn.StartGame();
		scn.MoveCompanionsToTable(thorin);
		scn.AttachCardsTo(bilbo, pony);
		scn.MoveMinionsToTable(footman);
		assertAttachedTo(pony, bilbo);

		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(thorin, footman);
		scn.FreepsResolveSkirmish(thorin);

		// A skirmish is under way and Bilbo could exert, but he is assigned to none.
		assertFalse(scn.IsCharAssigned(bilbo));
		assertEquals(0, scn.GetWoundsOn(bilbo));
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
		assertFalse(scn.FreepsActionAvailable(pony));
	}

	@Test
	public void SkirmishActionIsNotOfferedWhenBilboCannotExert() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var pony = scn.GetFreepsCard("pony");
		var bilbo = scn.GetRingBearer();
		var thorin = scn.GetFreepsCard("thorin");
		var pippin = scn.GetFreepsCard("pippin");
		var footman = scn.GetShadowCard("footman");
		var bats = scn.GetShadowCard("bats");

		scn.StartGame();
		scn.MoveCompanionsToTable(thorin, pippin);
		scn.AttachCardsTo(bilbo, pony);
		scn.MoveMinionsToTable(footman, bats);
		// Vitality 4, 3 wounds: Bilbo is exhausted.
		scn.AddWoundsToChar(bilbo, 3);
		assertEquals(3, scn.GetWoundsOn(bilbo));
		assertAttachedTo(pony, bilbo);

		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(new PhysicalCardImpl[]{bilbo, bats}, new PhysicalCardImpl[]{thorin, footman});
		scn.FreepsResolveSkirmish(thorin);

		// Bilbo is assigned but cannot exert; Pippin, a [shire] Hobbit companion who could, does not pay for him.
		assertTrue(scn.IsCharAssigned(bilbo));
		assertEquals(0, scn.GetWoundsOn(pippin));
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
		assertFalse(scn.FreepsActionAvailable(pony));
	}

	@Test
	public void InBilbosOwnSkirmishWithNoDwarvenCompanionBilboExertsForNothing() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var pony = scn.GetFreepsCard("pony");
		var bilbo = scn.GetRingBearer();
		var gandalf = scn.GetFreepsCard("gandalf");
		var army = scn.GetFreepsCard("army");
		var bats = scn.GetShadowCard("bats");

		scn.StartGame();
		scn.MoveCompanionsToTable(gandalf);
		scn.MoveAlliesToTable(army);
		scn.AttachCardsTo(bilbo, pony);
		scn.MoveMinionsToTable(bats);
		assertAttachedTo(pony, bilbo);

		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(bilbo, bats);
		scn.FreepsResolveSkirmish(bilbo);
		assertEquals(0, scn.GetWoundsOn(bilbo));
		assertEquals(7, scn.GetStrength(gandalf));
		assertEquals(9, scn.GetStrength(army));

		// Bilbo is skirmishing, in the skirmish he was assigned to. No [dwarven] companion: offered, paid, nothing.
		assertTrue(scn.FreepsActionAvailable(pony));
		scn.FreepsUseCardAction(pony);
		assertEquals(1, scn.GetWoundsOn(bilbo));
		assertEquals(7, scn.GetStrength(gandalf));
		assertEquals(9, scn.GetStrength(army));
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());
	}
}

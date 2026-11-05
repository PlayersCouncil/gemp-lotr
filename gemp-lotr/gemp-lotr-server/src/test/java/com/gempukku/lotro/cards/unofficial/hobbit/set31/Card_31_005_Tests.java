package com.gempukku.lotro.cards.unofficial.hobbit.set31;

import com.gempukku.lotro.common.*;
import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.game.PhysicalCardImpl;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_31_005_Tests
{
	// The Hobbit table: the Hobbit path and Bilbo, Reliable Companion 30_46 as the Ring-bearer, the one Bilbo in play.
	// The skirmishes of turn 1 are at The Troll Hoard (site 2); the minions are laid in the Shadow phase.
	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("coat", "31_5");

					put("balin", "30_2");       // Balin, Brother of Dwalin: [dwarven] companion, strength 7; a Fellowship ability not used
					put("legolas", "1_51");     // Legolas, Prince of Mirkwood: an archer companion (the Hobbit sets print no archer companion who adds to the total)
					put("fury", "30_3");        // Battle Fury: a [dwarven] Skirmish event, cost 1 (a Free Peoples event)

					put("narzug", "30_36");     // Narzug, Orkish Assassin: [gundabad] archer minion; his leave-play offer not reached
					put("archer", "33_38");     // Goblin Archer: [moria] archer minion; his play trigger not reached (cheated in)
					put("bats", "32_30");       // Dark Bats: [moria] Creature, strength 4, vitality 2: beats Bilbo and leaves him alive
					put("spider", "31_55");     // Wicked Spider: [spider] Spider, strength 7, fierce; its Skirmish ability not used
					put("anger", "30_32");      // Anger: a [gundabad] Skirmish event, cost 1
					put("ifhe", "31_21");       // If He Loses: a [gollum] Skirmish event, cost 2
					put("underling", "33_50");  // Orc Underling: a [sauron] minion of cost 1, a Shadow card that is no event
				}},
				null, null, null,
				VirtualTableScenario.HobbitDraft
		);
	}

	@Test
	public void MithrilCoatStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 31
		 * Name: Mithril Coat
		 * Unique: True
		 * Side: Free Peoples
		 * Culture: Dwarven
		 * Twilight Cost: 2
		 * Type: Artifact
		 * Subtype: Armor
		 * Vitality: +1
		 * Game Text: Bearer must be Bilbo.
		 * 	The minion archery total is -1.
		 * 	While Bilbo is skirmishing, the twilight cost of each Shadow event is +2.
		 */

		var scn = GetScenario();

		var card = scn.GetFreepsCard("coat");

		assertEquals("Mithril Coat", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertTrue(card.getBlueprint().isUnique());
		assertEquals(Side.FREE_PEOPLE, card.getBlueprint().getSide());
		assertEquals(Culture.DWARVEN, card.getBlueprint().getCulture());
		assertEquals(CardType.ARTIFACT, card.getBlueprint().getCardType());
		assertTrue(card.getBlueprint().getPossessionClasses().contains(PossessionClass.ARMOR));
		assertEquals(2, card.getBlueprint().getTwilightCost());
		assertEquals(1, card.getBlueprint().getVitality());
	}

	@Test
	public void PlaysOnBilboAndMakesTheMinionArcheryTotalMinusOneAndNotTheFellowships() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var coat = scn.GetFreepsCard("coat");
		var balin = scn.GetFreepsCard("balin");
		var legolas = scn.GetFreepsCard("legolas");
		var narzug = scn.GetShadowCard("narzug");
		var archer = scn.GetShadowCard("archer");
		var bilbo = scn.GetRingBearer();
		scn.MoveCompanionsToTable(balin, legolas);

		scn.StartGame();
		// Two archer minions, laid early: they stand on the table with the Coat later in the turn.
		scn.MoveMinionsToTable(narzug, archer);
		scn.MoveCardsToHand(coat);
		scn.FreepsRefreshDecision();
		assertInHand(coat);
		assertEquals(2, scn.GetShadowArcheryTotal());
		assertEquals(1, scn.GetFreepsArcheryTotal());

		assertTrue(scn.FreepsPlayAvailable(coat));
		scn.FreepsPlayCard(coat);
		// Bilbo is the one Bilbo in play: the Coat goes on him unasked, and not on Balin.
		assertAttachedTo(coat, bilbo);
		assertEquals(0, scn.GetAttachedCards(balin).size());

		// 2 archers - 1; -2 would read 0. The fellowship's total is untouched.
		assertEquals(1, scn.GetShadowArcheryTotal());
		assertEquals(1, scn.GetFreepsArcheryTotal());
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void EachShadowEventCostsTwoMoreOnlyWhileBilboIsSkirmishing() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var coat = scn.GetFreepsCard("coat");
		var balin = scn.GetFreepsCard("balin");
		var fury = scn.GetFreepsCard("fury");
		var bats = scn.GetShadowCard("bats");
		var spider = scn.GetShadowCard("spider");
		var anger = scn.GetShadowCard("anger");
		var ifhe = scn.GetShadowCard("ifhe");
		var underling = scn.GetShadowCard("underling");
		var bilbo = scn.GetRingBearer();
		scn.MoveCompanionsToTable(balin);

		scn.StartGame();
		scn.AttachCardsTo(bilbo, coat);
		assertAttachedTo(coat, bilbo);
		scn.MoveCardsToHand(fury, anger, ifhe, underling);
		assertInHand(fury, anger, ifhe, underling);
		scn.FreepsPass();
		scn.MoveMinionsToTable(bats, spider);

		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(new PhysicalCardImpl[]{balin, spider}, new PhysicalCardImpl[]{bilbo, bats});
		assertTrue(scn.FreepsAwaitingSkirmishChoice());

		// Balin's skirmish first: Bilbo is assigned and waits, and is not skirmishing.
		scn.FreepsResolveSkirmish(balin);
		assertTrue(scn.IsCharSkirmishingAgainst(balin, spider));
		assertFalse(scn.IsCharSkirmishing(bilbo));
		assertEquals(1, scn.GetTwilightCost(anger));
		assertEquals(2, scn.GetTwilightCost(ifhe));

		scn.PassSkirmishActions();
		scn.FreepsResolveSkirmish(bilbo);
		assertTrue(scn.IsCharSkirmishingAgainst(bilbo, bats));
		// Shadow events of two cultures, each +2.
		assertEquals(3, scn.GetTwilightCost(anger));
		assertEquals(4, scn.GetTwilightCost(ifhe));
		// A Shadow card that is no event, and a Free Peoples event: their printed costs.
		assertEquals(1, scn.GetTwilightCost(underling));
		assertEquals(1, scn.GetTwilightCost(fury));

		scn.SetTwilight(5);
		scn.FreepsPass();
		assertTrue(scn.ShadowPlayAvailable(anger));
		scn.ShadowPlayCard(anger);
		// 5 - (1 + 2), paid before Anger's minion is chosen.
		assertEquals(2, scn.GetTwilight());
		scn.ShadowChooseCard(bats);
		assertInDiscard(anger);
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
	}
}

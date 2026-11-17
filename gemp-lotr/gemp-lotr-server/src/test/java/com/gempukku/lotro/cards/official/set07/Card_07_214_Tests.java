package com.gempukku.lotro.cards.official.set07;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static org.junit.Assert.*;
import static com.gempukku.lotro.framework.Assertions.*;

public class Card_07_214_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("lemenya", "7_214");
					put("aragorn", "1_89");     // Aragorn, Ranger of the North: bears the Ranger's Sword
					put("sword", "1_112");      // Ranger's Sword: a [gondor] possession, borne
					put("toby", "1_305");       // Old Toby: a [shire] possession that nobody bears (support area)
					put("poison", "8_28");      // Spider Poison: a Shadow [gollum] possession in the Shadow support area
					put("shards", "3_44");      // The Shards of Narsil: a Free Peoples artifact, no possession
					// Cards for the Free Peoples hand, which decides who has initiative.
					put("hand1", "1_13");
					put("hand2", "1_13");
					put("hand3", "1_13");
					put("hand4", "1_13");
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void UlaireLemenyaStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 7
		 * Name: Úlairë Lemenya, Wraith on Wings
		 * Unique: True
		 * Side: Shadow
		 * Culture: Wraith
		 * Twilight Cost: 4
		 * Type: Minion
		 * Subtype: Nazgûl
		 * Strength: 9
		 * Vitality: 2
		 * Site Number: 3
		 * Game Text: <b>Regroup:</b> If you have initiative, discard Úlairë Lemenya to discard a possession.
		*/

		var scn = GetScenario();

		var lemenya = scn.GetFreepsCard("lemenya");

		assertEquals("Úlairë Lemenya", lemenya.getBlueprint().getTitle());
		assertEquals("Wraith on Wings", lemenya.getBlueprint().getSubtitle());
		assertTrue(lemenya.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, lemenya.getBlueprint().getSide());
		assertEquals(Culture.WRAITH, lemenya.getBlueprint().getCulture());
		assertEquals(CardType.MINION, lemenya.getBlueprint().getCardType());
		assertEquals(Race.NAZGUL, lemenya.getBlueprint().getRace());
		assertEquals(4, lemenya.getBlueprint().getTwilightCost());
		assertEquals(9, lemenya.getBlueprint().getStrength());
		assertEquals(2, lemenya.getBlueprint().getVitality());
		assertEquals(3, lemenya.getBlueprint().getSiteNumber());
	}

	@Test
	public void WithInitiativeRegroupAbilityDiscardsHerToDiscardOnePossessionOfEitherSide() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var lemenya = scn.GetShadowCard("lemenya");
		var poison = scn.GetShadowCard("poison");
		var aragorn = scn.GetFreepsCard("aragorn");
		var sword = scn.GetFreepsCard("sword");
		var toby = scn.GetFreepsCard("toby");
		var shards = scn.GetFreepsCard("shards");
		scn.MoveCompanionsToTable(aragorn);
		scn.AttachCardsTo(aragorn, sword);
		scn.MoveCardsToSupportArea(toby, shards, poison);
		assertAttachedTo(sword, aragorn);
		assertInZone(Zone.SUPPORT, toby, shards, poison);

		scn.StartGame();
		// No minion in the Shadow phase: straight on to the regroup phase.
		scn.FreepsPass();
		scn.SkipToPhase(Phase.REGROUP);
		scn.MoveMinionsToTable(lemenya);
		// Three cards in the Free Peoples hand: the Shadow has initiative.
		scn.MoveCardsToHand(scn.GetFreepsCard("hand1"), scn.GetFreepsCard("hand2"), scn.GetFreepsCard("hand3"));
		scn.FreepsPass();

		assertEquals(3, scn.GetFreepsHandCount());
		assertTrue(scn.ShadowHasInitiative());
		assertTrue(scn.AwaitingShadowRegroupPhaseActions());
		assertTrue(scn.ShadowActionAvailable(lemenya));

		scn.ShadowUseCardAction(lemenya);
		assertInDiscard(lemenya);
		// A possession of either side, borne or in a support area; not the artifact.
		assertTrue(scn.ShadowHasCardChoiceAvailable(sword, toby, poison));
		assertTrue(scn.ShadowHasCardChoiceNotAvailable(shards));
		assertEquals(1, scn.ShadowGetChoiceMin());
		scn.ShadowChooseCard(sword);

		assertInDiscard(sword);
		assertInZone(Zone.SUPPORT, toby, shards, poison);
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void WithFourCardsInTheFreePeoplesHandRegroupAbilityIsNotOffered() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var lemenya = scn.GetShadowCard("lemenya");
		var aragorn = scn.GetFreepsCard("aragorn");
		var sword = scn.GetFreepsCard("sword");
		scn.MoveCompanionsToTable(aragorn);
		scn.AttachCardsTo(aragorn, sword);
		assertAttachedTo(sword, aragorn);

		scn.StartGame();
		scn.FreepsPass();
		scn.SkipToPhase(Phase.REGROUP);
		scn.MoveMinionsToTable(lemenya);
		// Four cards in the Free Peoples hand: the Free Peoples player has initiative.
		scn.MoveCardsToHand(scn.GetFreepsCard("hand1"), scn.GetFreepsCard("hand2"),
				scn.GetFreepsCard("hand3"), scn.GetFreepsCard("hand4"));
		scn.FreepsPass();

		assertEquals(4, scn.GetFreepsHandCount());
		assertTrue(scn.FreepsHasInitiative());
		assertTrue(scn.AwaitingShadowRegroupPhaseActions());
		assertFalse(scn.ShadowActionAvailable(lemenya));
		assertInZone(Zone.SHADOW_CHARACTERS, lemenya);
		assertAttachedTo(sword, aragorn);
	}

	@Test
	public void WithNoPossessionInPlayRegroupAbilityDiscardsHerForNothing() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var lemenya = scn.GetShadowCard("lemenya");
		var aragorn = scn.GetFreepsCard("aragorn");
		var shards = scn.GetFreepsCard("shards");
		scn.MoveCompanionsToTable(aragorn);
		scn.MoveCardsToSupportArea(shards);
		assertInZone(Zone.SUPPORT, shards);

		scn.StartGame();
		scn.FreepsPass();
		scn.SkipToPhase(Phase.REGROUP);
		scn.MoveMinionsToTable(lemenya);
		scn.FreepsPass();

		assertEquals(0, scn.GetFreepsHandCount());
		assertTrue(scn.AwaitingShadowRegroupPhaseActions());
		// The discard of herself is a cost, so the ability is offered with nothing to discard.
		assertTrue(scn.ShadowActionAvailable(lemenya));

		scn.ShadowUseCardAction(lemenya);
		assertInDiscard(lemenya);
		assertInZone(Zone.SUPPORT, shards);
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}
}

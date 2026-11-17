package com.gempukku.lotro.cards.unofficial.hobbit.set30;

import com.gempukku.lotro.common.*;
import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_30_039_Tests
{

	// The Hobbit table: the Hobbit path and Bilbo, Reliable Companion 30_46 as the Ring-bearer.
	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("aggressor", "30_39");
					put("aggressor2", "30_39");
					// Thorin, Oakenshield: [dwarven] companion; a Fellowship ability only
					put("thorin", "30_19");
					// Threatening Warg: "Bearer must be a [gundabad] Orc. Bearer is fierce. Assignment: Exert bearer
					// twice or remove 2 doubts to assign bearer to Thorin."
					put("warg", "31_26");
				}},
				null, null, null,
				VirtualTableScenario.HobbitDraft
		);
	}

	@Test
	public void OrkishAggressorStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 30
		 * Name: Orkish Aggressor
		 * Unique: False
		 * Side: Shadow
		 * Culture: Gundabad
		 * Twilight Cost: 2
		 * Type: Minion
		 * Subtype: Orc
		 * Strength: 6
		 * Vitality: 2
		 * Site Number: 3
		 * Game Text: <b>Assignment:</b> Assign this minion to skirmish Bilbo. The Free Peoples player may add a doubt
		 * 	to discard this minion.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("aggressor");

		assertEquals("Orkish Aggressor", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, card.getBlueprint().getSide());
		assertEquals(Culture.GUNDABAD, card.getBlueprint().getCulture());
		assertEquals(CardType.MINION, card.getBlueprint().getCardType());
		assertEquals(Race.ORC, card.getBlueprint().getRace());
		assertEquals(2, card.getBlueprint().getTwilightCost());
		assertEquals(6, card.getBlueprint().getStrength());
		assertEquals(2, card.getBlueprint().getVitality());
		assertEquals(3, card.getBlueprint().getSiteNumber());
	}

	@Test
	public void AssignsItselfToBilboAndADoubtDiscardsIt() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var bilbo = scn.GetRingBearer();
		var thorin = scn.GetFreepsCard("thorin");
		scn.MoveCompanionsToTable(thorin);

		var aggressor = scn.GetShadowCard("aggressor");
		var aggressor2 = scn.GetShadowCard("aggressor2");

		scn.StartGame();
		scn.MoveMinionsToTable(aggressor, aggressor2);
		scn.SkipToPhase(Phase.ASSIGNMENT);
		scn.FreepsPass();

		assertTrue(scn.AwaitingShadowAssignmentPhaseActions());
		assertTrue(scn.ShadowActionAvailable(aggressor));
		scn.ShadowUseCardAction(aggressor);

		// Bilbo, with nothing asked; Thorin is not taken, and the other copy stays where it was.
		assertTrue(scn.IsCharAssignedAgainst(bilbo, aggressor));
		assertFalse(scn.IsCharAssigned(thorin));
		assertFalse(scn.IsCharAssigned(aggressor2));

		assertEquals(0, scn.GetBurdens());
		assertTrue(scn.FreepsDecisionAvailable("Would you like to add a burden to discard"));
		scn.FreepsChooseYes();

		// The doubt is the HDG's burden.
		assertEquals(1, scn.GetBurdens());
		assertInDiscard(aggressor);
		assertFalse(scn.IsCharAssigned(bilbo));
		assertInPlay(aggressor2);
		assertTrue(scn.AwaitingFreepsAssignmentPhaseActions());
	}

	@Test
	public void DeclinedTheDoubtItStaysOnBilbo() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var bilbo = scn.GetRingBearer();
		var aggressor = scn.GetShadowCard("aggressor");

		scn.StartGame();
		scn.MoveMinionsToTable(aggressor);
		scn.SkipToPhase(Phase.ASSIGNMENT);
		scn.FreepsPass();

		scn.ShadowUseCardAction(aggressor);
		assertTrue(scn.IsCharAssignedAgainst(bilbo, aggressor));
		assertEquals(0, scn.GetBurdens());
		assertTrue(scn.FreepsDecisionAvailable("Would you like to add a burden to discard"));
		scn.FreepsChooseNo();

		assertEquals(0, scn.GetBurdens());
		assertInPlay(aggressor);
		assertTrue(scn.IsCharAssignedAgainst(bilbo, aggressor));
		assertTrue(scn.AwaitingFreepsAssignmentPhaseActions());
	}

	@Test
	public void TheOtherCopyIsNotOfferedOnceBilboIsAssigned() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var bilbo = scn.GetRingBearer();
		var thorin = scn.GetFreepsCard("thorin");
		scn.MoveCompanionsToTable(thorin);

		var aggressor = scn.GetShadowCard("aggressor");
		var aggressor2 = scn.GetShadowCard("aggressor2");

		scn.StartGame();
		scn.MoveMinionsToTable(aggressor, aggressor2);
		scn.SkipToPhase(Phase.ASSIGNMENT);
		scn.FreepsPass();

		scn.ShadowUseCardAction(aggressor);
		assertTrue(scn.IsCharAssignedAgainst(bilbo, aggressor));
		assertTrue(scn.FreepsDecisionAvailable("Would you like to add a burden to discard"));
		scn.FreepsChooseNo();
		assertTrue(scn.IsCharAssignedAgainst(bilbo, aggressor));
		assertFalse(scn.IsCharAssigned(aggressor2));

		assertTrue(scn.AwaitingFreepsAssignmentPhaseActions());
		scn.FreepsPass();

		// Bilbo is taken: the second copy has nobody it could go to (CR 4.2 "assignment phase").
		assertTrue(scn.AwaitingShadowAssignmentPhaseActions());
		assertFalse(scn.ShadowActionAvailable(aggressor2));
		assertFalse(scn.ShadowActionAvailable(aggressor));
		assertFalse(scn.IsCharAssigned(aggressor2));
	}

	@Test
	public void NotOfferedOnceItIsAssignedToAnotherCompanion() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var bilbo = scn.GetRingBearer();
		var thorin = scn.GetFreepsCard("thorin");
		scn.MoveCompanionsToTable(thorin);

		var aggressor = scn.GetShadowCard("aggressor");
		var warg = scn.GetShadowCard("warg");

		scn.StartGame();
		scn.MoveMinionsToTable(aggressor);
		scn.AttachCardsTo(aggressor, warg);
		assertAttachedTo(warg, aggressor);
		scn.SkipToPhase(Phase.ASSIGNMENT);
		scn.FreepsPass();

		// The Warg's ability puts its bearer on Thorin (no doubts to remove: the exertions are paid).
		assertTrue(scn.ShadowActionAvailable(aggressor));
		scn.ShadowUseCardAction(warg);
		assertTrue(scn.IsCharAssignedAgainst(thorin, aggressor));
		assertEquals(2, scn.GetWoundsOn(aggressor));
		scn.FreepsPass();

		// Bilbo is still unassigned, but this minion no longer is.
		assertTrue(scn.AwaitingShadowAssignmentPhaseActions());
		assertFalse(scn.IsCharAssigned(bilbo));
		assertFalse(scn.ShadowActionAvailable(aggressor));
	}

	@Test
	public void NotOfferedInTheArcheryPhase() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var bilbo = scn.GetRingBearer();
		var aggressor = scn.GetShadowCard("aggressor");

		scn.StartGame();
		scn.MoveMinionsToTable(aggressor);
		scn.SkipToPhase(Phase.ARCHERY);
		scn.FreepsPass();

		assertTrue(scn.AwaitingShadowArcheryPhaseActions());
		assertFalse(scn.ShadowActionAvailable(aggressor));
		assertFalse(scn.IsCharAssigned(bilbo));
	}
}

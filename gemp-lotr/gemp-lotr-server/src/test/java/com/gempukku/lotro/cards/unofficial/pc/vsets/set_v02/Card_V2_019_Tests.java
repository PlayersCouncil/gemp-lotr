package com.gempukku.lotro.cards.unofficial.pc.vsets.set_v02;

import com.gempukku.lotro.cards.GenericCardTestHelper;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.game.PhysicalCardImpl;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static org.junit.Assert.*;

public class Card_V2_019_Tests
{

	protected GenericCardTestHelper GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new GenericCardTestHelper(
				new HashMap<>()
				{{
					put("card", "102_19");
					put("rangerComp", "1_96");
					put("gondorCardToChuck", "1_113");
					put("threeVitMan", "4_154");
					put("threeVitNonMan", "3_62");
					put("nonRoamingMinion", "1_233");
					// put other cards in here as needed for the test case
				}},
				GenericCardTestHelper.FellowshipSites,
				GenericCardTestHelper.FOTRFrodo,
				GenericCardTestHelper.RulingRing
		);
	}

	@Test
	public void DunadanofIthilienStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: V2
		 * Name: Dunadan of Ithilien
		 * Unique: True
		 * Side: Free Peoples
		 * Culture: Gondor
		 * Twilight Cost: 3
		 * Type: Companion
		 * Subtype: Man
		 * Strength: 6
		 * Vitality: 3
		 * Resistance: 6
		 * Signet: Frodo
		 * Game Text: Ranger. To play, spot a ranger.
		* 	Skirmish: Discard a [Gondor] card from hand to wound a roaming minion skirmishing this companion. If that minion is a Man, wound it again.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("card");

		assertEquals("Dunadan of Ithilien", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertTrue(card.getBlueprint().isUnique());
		assertEquals(Side.FREE_PEOPLE, card.getBlueprint().getSide());
		assertEquals(Culture.GONDOR, card.getBlueprint().getCulture());
		assertEquals(CardType.COMPANION, card.getBlueprint().getCardType());
		assertEquals(Race.MAN, card.getBlueprint().getRace());
		assertTrue(scn.hasKeyword(card, Keyword.RANGER));
		assertEquals(3, card.getBlueprint().getTwilightCost());
		assertEquals(6, card.getBlueprint().getStrength());
		assertEquals(3, card.getBlueprint().getVitality());
		assertEquals(6, card.getBlueprint().getResistance());
		assertEquals(Signet.FRODO, card.getBlueprint().getSignet()); 
	}


	@Test
	public void dumpsAGondorCardToWoundRoamingNonManMinionSkirmishingHimOnce() throws DecisionResultInvalidException, CardNotFoundException {
		//Pre-game setup
		var scn = GetScenario();

		var card = scn.GetFreepsCard("card");
		var rangerComp = scn.GetFreepsCard("rangerComp");
		var gondorCardToChuck = scn.GetFreepsCard("gondorCardToChuck");
		var threeVitNonMan = scn.GetShadowCard("threeVitNonMan");

		scn.FreepsMoveCardToHand(card, rangerComp, gondorCardToChuck);
		scn.ShadowMoveCardToHand(threeVitNonMan);

		scn.StartGame();
		scn.FreepsPlayCard(rangerComp);
		scn.FreepsPlayCard(card);

		assertEquals(6, scn.GetTwilight());

		scn.SkipToPhase(Phase.SHADOW);

		scn.ShadowPlayCard(threeVitNonMan);

		scn.SkipToPhase(Phase.ASSIGNMENT);

		scn.PassCurrentPhaseActions();
		scn.FreepsAssignToMinions(card, threeVitNonMan);
		scn.FreepsResolveSkirmish(card);

		assertTrue(scn.FreepsActionAvailable(card));
		assertEquals(0, scn.GetWoundsOn(threeVitNonMan));
		assertEquals(1, scn.GetFreepsHandCount());

		scn.FreepsUseCardAction(card);

		assertEquals(1, scn.GetWoundsOn(threeVitNonMan));
		assertEquals(0, scn.GetFreepsHandCount());
	}


	@Test
	public void dumpsAGondorCardToWoundRoamingManMinionSkirmishingHimTwice() throws DecisionResultInvalidException, CardNotFoundException {
		//Pre-game setup
		var scn = GetScenario();

		var card = scn.GetFreepsCard("card");
		var rangerComp = scn.GetFreepsCard("rangerComp");
		var gondorCardToChuck = scn.GetFreepsCard("gondorCardToChuck");
		var threeVitMan = scn.GetShadowCard("threeVitMan");

		scn.FreepsMoveCardToHand(card, rangerComp, gondorCardToChuck);
		scn.ShadowMoveCardToHand(threeVitMan);

		scn.StartGame();
		scn.FreepsPlayCard(rangerComp);
		scn.FreepsPlayCard(card);

		assertEquals(6, scn.GetTwilight());

		scn.SkipToPhase(Phase.SHADOW);

		scn.ShadowPlayCard(threeVitMan);

		scn.SkipToPhase(Phase.ASSIGNMENT);

		scn.PassCurrentPhaseActions();
		scn.FreepsAssignToMinions(card, threeVitMan);
		scn.FreepsResolveSkirmish(card);

		assertTrue(scn.FreepsActionAvailable(card));
		assertEquals(0, scn.GetWoundsOn(threeVitMan));
		assertEquals(1, scn.GetFreepsHandCount());

		scn.FreepsUseCardAction(card);

		assertEquals(2, scn.GetWoundsOn(threeVitMan));
		assertEquals(0, scn.GetFreepsHandCount());
	}


	@Test
	public void dumpsAGondorCardForNoEffectAgainstNonRoamingMinion() throws DecisionResultInvalidException, CardNotFoundException {
		//Pre-game setup
		var scn = GetScenario();

		var card = scn.GetFreepsCard("card");
		var rangerComp = scn.GetFreepsCard("rangerComp");
		var gondorCardToChuck = scn.GetFreepsCard("gondorCardToChuck");
		var nonRoamingMinion = scn.GetShadowCard("nonRoamingMinion");

		scn.FreepsMoveCardToHand(card, rangerComp, gondorCardToChuck);
		scn.ShadowMoveCardToHand(nonRoamingMinion);

		scn.StartGame();
		scn.FreepsPlayCard(rangerComp);
		scn.FreepsPlayCard(card);

		assertEquals(6, scn.GetTwilight());

		scn.SkipToPhase(Phase.SHADOW);

		scn.ShadowPlayCard(nonRoamingMinion);

		scn.SkipToPhase(Phase.ASSIGNMENT);

		scn.PassCurrentPhaseActions();
		scn.FreepsAssignToMinions(card, nonRoamingMinion);
		scn.FreepsResolveSkirmish(card);

		assertTrue(scn.FreepsActionAvailable(card));
		assertEquals(0, scn.GetWoundsOn(nonRoamingMinion));
		assertEquals(1, scn.GetFreepsHandCount());

		scn.FreepsUseCardAction(card);

		assertEquals(0, scn.GetWoundsOn(nonRoamingMinion));
		assertEquals(0, scn.GetFreepsHandCount());
	}



	@Test
	public void dumpsAGondorCardForNoEffectWhenRoamingMinionIsSkirmishingOtherCompanion() throws DecisionResultInvalidException, CardNotFoundException {
		//Pre-game setup
		var scn = GetScenario();

		var card = scn.GetFreepsCard("card");
		var rangerComp = scn.GetFreepsCard("rangerComp");
		var gondorCardToChuck = scn.GetFreepsCard("gondorCardToChuck");
		var threeVitMan = scn.GetShadowCard("threeVitMan");

		scn.FreepsMoveCardToHand(card, rangerComp, gondorCardToChuck);
		scn.ShadowMoveCardToHand(threeVitMan);

		scn.StartGame();
		scn.FreepsPlayCard(rangerComp);
		scn.FreepsPlayCard(card);

		assertEquals(6, scn.GetTwilight());

		scn.SkipToPhase(Phase.SHADOW);

		scn.ShadowPlayCard(threeVitMan);

		scn.SkipToPhase(Phase.ASSIGNMENT);

		scn.PassCurrentPhaseActions();
		scn.FreepsAssignToMinions(rangerComp, threeVitMan);
		scn.FreepsResolveSkirmish(rangerComp);

		assertTrue(scn.FreepsActionAvailable(card));
		assertEquals(0, scn.GetWoundsOn(threeVitMan));
		assertEquals(1, scn.GetFreepsHandCount());

		scn.FreepsUseCardAction(card);

		assertEquals(0, scn.GetFreepsHandCount());
	}
}

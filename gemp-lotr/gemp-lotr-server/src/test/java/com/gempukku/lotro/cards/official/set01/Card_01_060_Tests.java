package com.gempukku.lotro.cards.official.set01;

import com.gempukku.lotro.common.*;
import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.game.AbstractActionProxy;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.game.state.LotroGame;
import com.gempukku.lotro.logic.actions.ActivateCardAction;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import com.gempukku.lotro.logic.effects.PlaySiteEffect;
import com.gempukku.lotro.logic.timing.Action;
import org.junit.Test;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;

import static org.junit.Assert.*;

public class Card_01_060_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("silinde", "1_60");
					put("arwen", "1_30");
					put("elrond", "1_40");
					put("pathfinder", "1_110");
				}},
				new HashMap<>() {{
					put("site1", "1_319");
					put("site2", "1_327");
					//Rivendell Valley
					put("site3", "1_341");
					put("site4", "1_343");
					put("site5", "1_349");
					put("site6", "1_350");
					put("site7", "1_353");
					put("site8", "1_356");
					put("site9", "1_360");
				}},
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void SilndeStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 1
		 * Name: Silinde, Elf of Mirkwood
		 * Unique: True
		 * Side: Free Peoples
		 * Culture: Elven
		 * Twilight Cost: 2
		 * Type: Ally
		 * Subtype: Elf
		 * Strength: 5
		 * Vitality: 2
		 * Site Number: 3
		 * Game Text: While you can spot your site 3, Silinde has the game text of that site.
		 */

		var scn = GetScenario();

		var card = scn.GetFreepsCard("silinde");

		assertEquals("Silinde", card.getBlueprint().getTitle());
		assertEquals("Elf of Mirkwood", card.getBlueprint().getSubtitle());
		assertTrue(card.getBlueprint().isUnique());
		assertEquals(Side.FREE_PEOPLE, card.getBlueprint().getSide());
		assertEquals(Culture.ELVEN, card.getBlueprint().getCulture());
		assertEquals(CardType.ALLY, card.getBlueprint().getCardType());
		assertEquals(Race.ELF, card.getBlueprint().getRace());
		assertEquals(2, card.getBlueprint().getTwilightCost());
		assertEquals(5, card.getBlueprint().getStrength());
		assertEquals(2, card.getBlueprint().getVitality());
		assertTrue(card.getBlueprint().hasAllyHome(new AllyHome(SitesBlock.FELLOWSHIP, 3)));
	}

	@Test
	public void SilindeCopiesYourSite3() throws DecisionResultInvalidException, CardNotFoundException {
		//Pre-game setup
		var scn = GetScenario();

		var silinde = scn.GetFreepsCard("silinde");
		var arwen = scn.GetFreepsCard("arwen");
		var elrond = scn.GetFreepsCard("elrond");
		var pathfinder = scn.GetFreepsCard("pathfinder");
		scn.MoveCardsToHand(elrond, pathfinder);
		scn.MoveCompanionsToTable(silinde, arwen);

		scn.StartGame();

		scn.SkipToSite(2);
		assertFalse(scn.FreepsActionAvailable(silinde));
		scn.FreepsPlayCard(pathfinder);

		//If everything worked, Silinde should have Rivendell Vally's game text:
		// Fellowship: Play an Elf to draw a card.
		assertTrue(scn.FreepsActionAvailable(silinde));
	}

	@Test
	public void SilindeDoesNotCopyOpponentSite3() throws DecisionResultInvalidException, CardNotFoundException {
		//Pre-game setup
		var scn = GetScenario();

		var silinde = scn.GetFreepsCard("silinde");
		var arwen = scn.GetFreepsCard("arwen");
		var elrond = scn.GetFreepsCard("elrond");
		var pathfinder = scn.GetFreepsCard("pathfinder");
		scn.MoveCardsToHand(elrond, pathfinder);
		scn.MoveCompanionsToTable(silinde, arwen);

		scn.StartGame();

		scn.SkipToSite(3);
		assertFalse(scn.FreepsActionAvailable(silinde));
	}

	@Test
	public void ReplacingYourSiteWithOpponentSiteStopsCopy() throws DecisionResultInvalidException, CardNotFoundException {
		//Pre-game setup
		var scn = GetScenario();

		var silinde = scn.GetFreepsCard("silinde");
		var arwen = scn.GetFreepsCard("arwen");
		var elrond = scn.GetFreepsCard("elrond");
		var pathfinder = scn.GetFreepsCard("pathfinder");
		scn.MoveCardsToHand(elrond, pathfinder);
		scn.MoveCompanionsToTable(silinde, arwen);

		scn.StartGame();

		scn.SkipToSite(2);
		scn.FreepsPlayCard(pathfinder);
		scn.MoveCardsToHand(pathfinder); //we cheat and put it back so we can use it later to reset the active decision
		scn.SkipToSite(3);

		//Cheats and adds an ability to Arwen that forces the Shadow player to play their site 3
		scn.ApplyAdHocAction(new AbstractActionProxy() {
			@Override
			public List<? extends Action> getPhaseActions(String playerId, LotroGame game) {
				ActivateCardAction action = new ActivateCardAction(arwen, VirtualTableScenario.P1);
				action.appendEffect(new PlaySiteEffect(action, VirtualTableScenario.P2, SitesBlock.FELLOWSHIP, 3));
				return Collections.singletonList(action);
			}
		});

		//uses it just to clear the decision tree
		scn.FreepsPlayCard(pathfinder);

		assertTrue(scn.FreepsActionAvailable(silinde));
		scn.FreepsUseCardAction(arwen);
		assertFalse(scn.FreepsActionAvailable(silinde));
	}

	@Test
	public void ReplacingOpponentSiteWithYourSiteStartsCopy() throws DecisionResultInvalidException, CardNotFoundException {
		//Pre-game setup
		var scn = GetScenario();

		var silinde = scn.GetFreepsCard("silinde");
		var arwen = scn.GetFreepsCard("arwen");
		var elrond = scn.GetFreepsCard("elrond");
		var pathfinder = scn.GetFreepsCard("pathfinder");
		scn.MoveCardsToHand(elrond, pathfinder);
		scn.MoveCompanionsToTable(silinde, arwen);

		scn.StartGame();

		scn.SkipToSite(3);

		//Cheats and adds an ability to Arwen that allows site 3 to be played
		scn.ApplyAdHocAction(new AbstractActionProxy() {
			@Override
			public List<? extends Action> getPhaseActions(String playerId, LotroGame game) {
				ActivateCardAction action = new ActivateCardAction(arwen, VirtualTableScenario.P1);
				action.appendEffect(new PlaySiteEffect(action, playerId, SitesBlock.FELLOWSHIP, 3));
				return Collections.singletonList(action);
			}
		});

		//uses it just to clear the decision tree
		scn.FreepsPlayCard(pathfinder);

		assertFalse(scn.FreepsActionAvailable(silinde));
		scn.FreepsUseCardAction(arwen);
		assertTrue(scn.FreepsActionAvailable(silinde));
	}
}

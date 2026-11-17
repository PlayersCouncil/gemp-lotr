package com.gempukku.lotro.cards.official.set02;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_02_065_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("spearman1", "2_65");
					put("spearman2", "2_65");
					// Goblin Spear: [moria] hand weapon, twilight 1; bearer must be a [moria] minion; its trigger
					// answers only a companion killed by its bearer
					put("spear", "1_182");
					put("spear2", "1_182");
					// Goblin Scimitar: [moria] hand weapon of another title; its trigger answers only its own play
					put("scimitar", "1_180");
					// Goblin Patrol Troop: [moria] Orc, no text
					put("troop", "1_177");
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void GoblinSpearmanStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 2
		 * Name: Goblin Spearman
		 * Unique: False
		 * Side: Shadow
		 * Culture: Moria
		 * Twilight Cost: 2
		 * Type: Minion
		 * Subtype: Orc
		 * Strength: 6
		 * Vitality: 1
		 * Site Number: 4
		 * Game Text: While bearing a Goblin Spear, this minion is <b>damage +2</b>.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("spearman1");

		assertEquals("Goblin Spearman", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, card.getBlueprint().getSide());
		assertEquals(Culture.MORIA, card.getBlueprint().getCulture());
		assertEquals(CardType.MINION, card.getBlueprint().getCardType());
		assertEquals(Race.ORC, card.getBlueprint().getRace());
		assertEquals(2, card.getBlueprint().getTwilightCost());
		assertEquals(6, card.getBlueprint().getStrength());
		assertEquals(1, card.getBlueprint().getVitality());
		assertEquals(4, card.getBlueprint().getSiteNumber());
	}

	@Test
	public void SpearmanIsDamagePlusTwoOnceAGoblinSpearIsPlayedOnHim() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var spearman1 = scn.GetShadowCard("spearman1");
		var spearman2 = scn.GetShadowCard("spearman2");
		var spear = scn.GetShadowCard("spear");

		scn.StartGame();
		scn.MoveCardsToHand(spear);
		assertInHand(spear);
		scn.FreepsPass(); // to site 2: the Shadow phase
		scn.MoveMinionsToTable(spearman1, spearman2);
		scn.SetTwilight(10);
		scn.ShadowRefreshDecision();

		assertEquals(0, scn.GetKeywordCount(spearman1, Keyword.DAMAGE));
		assertEquals(0, scn.GetKeywordCount(spearman2, Keyword.DAMAGE));

		scn.ShadowPlayCard(spear);
		assertTrue(scn.ShadowHasCardChoiceAvailable(spearman1, spearman2));
		scn.ShadowChooseCard(spearman1);

		assertAttachedTo(spear, spearman1);
		assertEquals(2, scn.GetKeywordCount(spearman1, Keyword.DAMAGE));
		// The other copy, bearing nothing, is untouched.
		assertEquals(0, scn.GetKeywordCount(spearman2, Keyword.DAMAGE));
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void AnotherWeaponGivesNothingAndAnotherMinionBearingAGoblinSpearGainsNothing() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var spearman = scn.GetShadowCard("spearman1");
		var scimitar = scn.GetShadowCard("scimitar");
		var troop = scn.GetShadowCard("troop");
		var spear = scn.GetShadowCard("spear2");

		scn.StartGame();
		scn.FreepsPass(); // to site 2: the Shadow phase
		scn.MoveMinionsToTable(spearman, troop);
		scn.AttachCardsTo(spearman, scimitar);
		scn.AttachCardsTo(troop, spear);

		assertAttachedTo(scimitar, spearman);
		assertAttachedTo(spear, troop);
		// A hand weapon of another title on him, and a Goblin Spear on another [moria] Orc beside him.
		assertEquals(0, scn.GetKeywordCount(spearman, Keyword.DAMAGE));
		assertEquals(0, scn.GetKeywordCount(troop, Keyword.DAMAGE));
		assertTrue(scn.AwaitingShadowPhaseActions());
	}
}

package com.gempukku.lotro.cards.official.set12;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.game.PhysicalCardImpl;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_12_178_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("nertea", "12_178");
					// Shadowy Mount: [wraith] mount, cost 2, vitality +1 and no strength; "Bearer must be a Nazgûl"; fierce
					// only at a battleground or forest, which no site of this path is, and an assignment ability used only on purpose
					put("shadowy", "12_171");
					// War-warg: [isengard] mount, cost 4; its response answers only a skirmish special ability
					put("warg", "5_64");
					// Horse of Rohan: a Free Peoples [rohan] mount, cost 2; read in hand only
					put("horse", "4_283");
					// Nazgûl Sword: [wraith] possession that is no mount, a hand weapon, cost 1, strength +2; "Bearer must be a
					// Nazgûl"; damage +1 only at 3 burdens
					put("sword", "1_218");
					// Úlairë Lemenya, Lieutenant of Morgul: a [wraith] Nazgûl, strength 9; his text reaches only a character
					// bearing a [wraith] condition, and none is laid
					put("lemenya", "1_232");
					// Ulaire Attea, Khamûl of the East: a [raider] Nazgûl, strength 12; the rest of his text needs 3 burdens
					put("khamul", "103_61");
					// Morgul Hound: a [wraith] minion who is an Orc and no Nazgûl; strength 5, nothing below 2 wounds on the
					// Ring-bearer
					put("hound", "7_192");
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void UlaireNerteaStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 12
		 * Name: Úlairë Nertëa, Black Horseman
		 * Unique: True
		 * Side: Shadow
		 * Culture: Wraith
		 * Twilight Cost: 4
		 * Type: Minion
		 * Subtype: Nazgûl
		 * Strength: 9
		 * Vitality: 2
		 * Site Number: 3
		 * Game Text: The twilight cost of each mount is -1.<br>While Úlairë Nertëa is mounted, each Nazgûl is strength +1 and has <b>muster</b>. <helper>(At the start of the regroup phase, you may discard a card from hand to draw a card.)</helper>
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("nertea");

		assertEquals("Úlairë Nertëa", card.getBlueprint().getTitle());
		assertEquals("Black Horseman", card.getBlueprint().getSubtitle());
		assertTrue(card.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, card.getBlueprint().getSide());
		assertEquals(Culture.WRAITH, card.getBlueprint().getCulture());
		assertEquals(CardType.MINION, card.getBlueprint().getCardType());
		assertEquals(Race.NAZGUL, card.getBlueprint().getRace());
		assertEquals(4, card.getBlueprint().getTwilightCost());
		assertEquals(9, card.getBlueprint().getStrength());
		assertEquals(2, card.getBlueprint().getVitality());
		assertEquals(3, card.getBlueprint().getSiteNumber());
	}

	@Test
	public void EachMountInEitherHandCostsOneLessWhileSheIsInPlayAndAPossessionThatIsNoMountDoesNot() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var nertea = scn.GetShadowCard("nertea");
		var shadowy = scn.GetShadowCard("shadowy");
		var warg = scn.GetShadowCard("warg");
		var sword = scn.GetShadowCard("sword");
		var horse = scn.GetFreepsCard("horse");
		scn.MoveCardsToHand(shadowy, warg, sword, horse);

		scn.StartGame();
		assertInHand(shadowy, warg, sword, horse);
		assertEquals(2, scn.GetTwilightCost(shadowy));
		assertEquals(4, scn.GetTwilightCost(warg));
		assertEquals(1, scn.GetTwilightCost(sword));
		assertEquals(2, scn.GetTwilightCost(horse));

		scn.MoveMinionsToTable(nertea);
		scn.FreepsRefreshDecision();
		assertEquals(1, scn.GetTwilightCost(shadowy));
		assertEquals(3, scn.GetTwilightCost(warg));
		assertEquals(1, scn.GetTwilightCost(sword));
		assertEquals(1, scn.GetTwilightCost(horse));
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void AMountPlayedOnHerCostsOneLessAndMakesEachNazgulStrongerWithMuster() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var nertea = scn.GetShadowCard("nertea");
		var shadowy = scn.GetShadowCard("shadowy");
		var lemenya = scn.GetShadowCard("lemenya");
		var khamul = scn.GetShadowCard("khamul");
		var hound = scn.GetShadowCard("hound");
		scn.MoveCardsToHand(shadowy);

		scn.StartGame();
		assertInHand(shadowy);
		scn.MoveMinionsToTable(nertea, lemenya, khamul, hound);
		scn.SetTwilight(10);
		scn.FreepsPass();

		assertEquals(9, scn.GetStrength(nertea));
		assertEquals(9, scn.GetStrength(lemenya));
		assertEquals(12, scn.GetStrength(khamul));
		assertEquals(5, scn.GetStrength(hound));
		assertFalse(scn.HasKeyword(nertea, Keyword.MUSTER));
		assertFalse(scn.HasKeyword(lemenya, Keyword.MUSTER));
		assertFalse(scn.HasKeyword(khamul, Keyword.MUSTER));
		assertFalse(scn.HasKeyword(hound, Keyword.MUSTER));
		int pool = scn.GetTwilight();

		scn.ShadowPlayCard(shadowy);
		scn.ShadowChooseCard(nertea);
		assertAttachedTo(shadowy, nertea);
		// 2 printed - 1
		assertEquals(pool - 1, scn.GetTwilight());

		// The Shadowy Mount gives no strength: each +1 is Nertëa's.
		assertEquals(10, scn.GetStrength(nertea));
		assertEquals(10, scn.GetStrength(lemenya));
		assertEquals(13, scn.GetStrength(khamul));
		assertEquals(5, scn.GetStrength(hound));
		assertTrue(scn.HasKeyword(nertea, Keyword.MUSTER));
		assertTrue(scn.HasKeyword(lemenya, Keyword.MUSTER));
		assertTrue(scn.HasKeyword(khamul, Keyword.MUSTER));
		assertFalse(scn.HasKeyword(hound, Keyword.MUSTER));
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void NoNazgulGainsWhileSheBearsAPossessionThatIsNoMountAndAnotherNazgulIsMounted() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var nertea = scn.GetShadowCard("nertea");
		var sword = scn.GetShadowCard("sword");
		var shadowy = scn.GetShadowCard("shadowy");
		var lemenya = scn.GetShadowCard("lemenya");
		var khamul = scn.GetShadowCard("khamul");

		scn.StartGame();
		scn.MoveMinionsToTable(nertea, lemenya, khamul);
		scn.AttachCardsTo(nertea, sword);
		scn.AttachCardsTo(lemenya, shadowy);
		assertAttachedTo(sword, nertea);
		assertAttachedTo(shadowy, lemenya);

		// 9 + 2 for the Sword, and nothing of her own
		assertEquals(11, scn.GetStrength(nertea));
		assertEquals(9, scn.GetStrength(lemenya));
		assertEquals(12, scn.GetStrength(khamul));
		assertFalse(scn.HasKeyword(nertea, Keyword.MUSTER));
		assertFalse(scn.HasKeyword(lemenya, Keyword.MUSTER));
		assertFalse(scn.HasKeyword(khamul, Keyword.MUSTER));
	}
}

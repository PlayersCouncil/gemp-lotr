package com.gempukku.lotro.cards.official.set01;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_01_219_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("servants", "1_219");
					put("lemenya", "1_232");    // Úlairë Lemenya, Lieutenant of Morgul: a [wraith] Nazgûl; his text reads only companions bearing [wraith] conditions
					put("khamul", "103_61");    // Ulaire Attea, Khamul of the East: a [raider] Nazgûl; his text needs 3 burdens
					put("hound", "7_192");      // Morgul Hound: a [wraith] Orc, no Nazgûl; his text needs 2 wounds on the Ring-bearer
					put("ted", "18_75");        // Ted Sandyman, Chief's Men's Ally: a Hobbit minion; his abilities are used only on purpose
					put("sam", "1_311");        // Sam, Son of Hamfast: a Ring-bound Hobbit who is not the Ring-bearer
					put("pippin", "103_31");    // Pippin, Trollslayer: a [gondor] Hobbit companion; a skirmish ability used only on purpose
					put("merry", "1_303");      // Merry, From O'er the Brandywine: a Hobbit companion, strength +2 only with a weapon
					put("bounder", "1_286");    // Bounder: a Hobbit ally; a skirmish ability used only on purpose
					put("aragorn", "1_89");     // Aragorn, Ranger of the North: a companion who is no Hobbit
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void TheNineServantsofSauronStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 1
		 * Name: The Nine Servants of Sauron
		 * Unique: False
		 * Side: Shadow
		 * Culture: Wraith
		 * Twilight Cost: 1
		 * Type: Condition
		 * Subtype: Support Area
		 * Game Text: <b>Search</b>. Plays to your support area.
		 * Each time you play a Nazgûl, you may exert a Hobbit (except the Ring-bearer).
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("servants");

		assertEquals("The Nine Servants of Sauron", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, card.getBlueprint().getSide());
		assertEquals(Culture.WRAITH, card.getBlueprint().getCulture());
		assertEquals(CardType.CONDITION, card.getBlueprint().getCardType());
		assertTrue(scn.HasKeyword(card, Keyword.SEARCH));
		assertTrue(scn.HasKeyword(card, Keyword.SUPPORT_AREA));
		assertEquals(1, card.getBlueprint().getTwilightCost());
	}

	@Test
	public void EachNazgulPlayedLetsYouExertAHobbitWhoIsNotTheRingBearer() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var servants = scn.GetShadowCard("servants");
		var lemenya = scn.GetShadowCard("lemenya");
		var khamul = scn.GetShadowCard("khamul");
		var ted = scn.GetShadowCard("ted");
		var frodo = scn.GetRingBearer();
		var sam = scn.GetFreepsCard("sam");
		var pippin = scn.GetFreepsCard("pippin");
		var merry = scn.GetFreepsCard("merry");
		var bounder = scn.GetFreepsCard("bounder");
		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCompanionsToTable(sam, pippin, merry, aragorn);
		scn.MoveAlliesToTable(bounder);

		scn.StartGame();

		// Vitality 4: with 3 wounds Merry cannot exert.
		scn.AddWoundsToChar(merry, 3);
		scn.MoveCardsToSupportArea(servants);
		scn.MoveMinionsToTable(ted);
		scn.MoveCardsToHand(lemenya, khamul);
		scn.SetTwilight(20);
		assertEquals(3, scn.GetWoundsOn(merry));

		scn.FreepsPass();

		assertEquals(0, scn.GetWoundsOn(frodo));
		assertEquals(0, scn.GetWoundsOn(sam));
		assertEquals(0, scn.GetWoundsOn(pippin));
		assertEquals(0, scn.GetWoundsOn(bounder));
		assertEquals(0, scn.GetWoundsOn(ted));
		assertEquals(0, scn.GetWoundsOn(aragorn));

		scn.ShadowPlayCard(lemenya);
		assertTrue(scn.ShadowHasOptionalTriggerAvailable(servants));
		scn.ShadowAcceptOptionalTrigger();

		// A Ring-bound companion, a Hobbit of another culture, a Hobbit ally and the Shadow player's own Hobbit
		// minion; not the Ring-bearer, a companion who is no Hobbit, or a Hobbit who cannot exert.
		assertTrue(scn.ShadowHasCardChoiceAvailable(sam, pippin, bounder, ted));
		assertTrue(scn.ShadowHasCardChoiceNotAvailable(frodo, aragorn, merry));
		assertEquals(1, scn.ShadowGetChoiceMin());
		scn.ShadowChooseCard(sam);

		assertEquals(1, scn.GetWoundsOn(sam));
		assertEquals(0, scn.GetWoundsOn(frodo));
		assertEquals(0, scn.GetWoundsOn(pippin));
		assertEquals(0, scn.GetWoundsOn(bounder));
		assertEquals(0, scn.GetWoundsOn(ted));
		assertEquals(0, scn.GetWoundsOn(aragorn));
		assertTrue(scn.AwaitingShadowPhaseActions());

		// A second Nazgûl, of another culture: the offer comes again, and the same Hobbit exerts again.
		scn.ShadowPlayCard(khamul);
		assertTrue(scn.ShadowHasOptionalTriggerAvailable(servants));
		scn.ShadowAcceptOptionalTrigger();
		scn.ShadowChooseCard(sam);

		assertEquals(2, scn.GetWoundsOn(sam));
		assertEquals(0, scn.GetWoundsOn(pippin));
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void DecliningTheOfferExertsNobody() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var servants = scn.GetShadowCard("servants");
		var lemenya = scn.GetShadowCard("lemenya");
		var sam = scn.GetFreepsCard("sam");
		var pippin = scn.GetFreepsCard("pippin");
		scn.MoveCompanionsToTable(sam, pippin);

		scn.StartGame();

		scn.MoveCardsToSupportArea(servants);
		scn.MoveCardsToHand(lemenya);
		scn.SetTwilight(20);

		scn.FreepsPass();

		assertEquals(0, scn.GetWoundsOn(sam));
		assertEquals(0, scn.GetWoundsOn(pippin));

		scn.ShadowPlayCard(lemenya);
		assertTrue(scn.ShadowHasOptionalTriggerAvailable(servants));
		scn.ShadowDeclineOptionalTrigger();

		assertEquals(0, scn.GetWoundsOn(sam));
		assertEquals(0, scn.GetWoundsOn(pippin));
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void NeitherItsOwnPlayNorAWraithMinionWhoIsNoNazgulSetsItOff() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var servants = scn.GetShadowCard("servants");
		var hound = scn.GetShadowCard("hound");
		var sam = scn.GetFreepsCard("sam");
		scn.MoveCompanionsToTable(sam);

		scn.StartGame();

		scn.MoveCardsToHand(servants, hound);
		scn.SetTwilight(20);

		scn.FreepsPass();

		assertEquals(0, scn.GetWoundsOn(sam));

		// A [wraith] card that is no Nazgûl: the condition itself, played to the support area.
		scn.ShadowPlayCard(servants);
		assertInZone(Zone.SUPPORT, servants);
		assertTrue(scn.AwaitingShadowPhaseActions());
		assertFalse(scn.ShadowHasOptionalTriggerAvailable(servants));
		assertEquals(0, scn.GetWoundsOn(sam));

		// A [wraith] minion who is an Orc and no Nazgûl.
		scn.ShadowPlayCard(hound);
		assertTrue(scn.AwaitingShadowPhaseActions());
		assertFalse(scn.ShadowHasOptionalTriggerAvailable(servants));
		assertEquals(0, scn.GetWoundsOn(sam));
	}

	@Test
	public void NotOfferedWhenNoHobbitButTheRingBearerCanExert() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var servants = scn.GetShadowCard("servants");
		var lemenya = scn.GetShadowCard("lemenya");
		var frodo = scn.GetRingBearer();
		var sam = scn.GetFreepsCard("sam");
		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCompanionsToTable(sam, aragorn);

		scn.StartGame();

		// Vitality 4: with 3 wounds Sam cannot exert. Frodo and Aragorn can, and are not Hobbits it may take.
		scn.AddWoundsToChar(sam, 3);
		scn.MoveCardsToSupportArea(servants);
		scn.MoveCardsToHand(lemenya);
		scn.SetTwilight(20);
		assertEquals(3, scn.GetWoundsOn(sam));

		scn.FreepsPass();

		assertEquals(0, scn.GetWoundsOn(frodo));
		assertEquals(0, scn.GetWoundsOn(aragorn));

		scn.ShadowPlayCard(lemenya);
		assertTrue(scn.AwaitingShadowPhaseActions());
		assertFalse(scn.ShadowHasOptionalTriggerAvailable(servants));
		assertEquals(0, scn.GetWoundsOn(frodo));
		assertEquals(3, scn.GetWoundsOn(sam));
		assertEquals(0, scn.GetWoundsOn(aragorn));
	}
}

package com.gempukku.lotro.cards.official.set13;

import com.gempukku.lotro.common.*;
import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.game.LotroCardBlueprint;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_13_191_Tests
{
	// The site itself on a Shadows-block path, as the first site, where the game starts.
	protected VirtualTableScenario GetShadowsScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("aragorn", "1_89");         // Aragorn, Ranger of the North: strength 8; a maneuver ability used only on purpose
					put("nostranger", "1_108");     // No Stranger to the Shadows: [gondor] condition on a ranger, no strength; each site's Shadow number -1
					put("worker", "15_135");        // Rohan Worker: a follower that changes no number; its transfer trigger is passed by the rig
					put("poison", "8_28");          // Spider Poison: a Shadow possession a companion bears, no strength; it answers his assignment
					put("sam", "1_311");            // Sam, Son of Hamfast: strength 3, bearing nothing
					put("gimli", "1_13");           // Gimli, Son of Gloin: strength 6; a skirmish ability used only on purpose
					put("armor", "1_8");            // Dwarven Armor: cost 0, bearer must be a Dwarf; changes no strength
					put("maggot", "1_288");         // Farmer Maggot, Chaser of Rascals: an ally, strength 2
					put("pipe", "1_285");           // Bilbo's Pipe: bearer must be a Hobbit; changes no strength
					put("troop", "1_177");          // Goblin Patrol Troop: [moria] Orc, strength 13, no text
					put("scimitar", "1_180");       // Goblin Scimitar: hand weapon for a [moria] Orc, strength +2
				}},
				new HashMap<>()
				{{
					put("site1", "13_191");         // Fords of Isen
					put("site2", "18_138");         // Sirannon Ruins: no text
					put("site3", "11_234");
					put("site4", "17_148");
					put("site5", "13_185");
					put("site6", "11_230");
					put("site7", "12_187");
					put("site8", "11_239");
					put("site9", "17_146");
				}},
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing,
				VirtualTableScenario.Shadows
		);
	}

	@Test
	public void FordsofIsenStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 13
		 * Name: Fords of Isen
		 * Unique: false
		 * Type: Site
		 * Block: Shadows
		 * Shadow Number: 2
		 * Direction: Left
		 * Game Text: <b>River</b>. Each character is strength +1 for each card borne by that character.
		 */

		var scn = GetShadowsScenario();

		var card = scn.GetFreepsSite("site1");

		assertEquals("Fords of Isen", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(CardType.SITE, card.getBlueprint().getCardType());
		assertEquals(SitesBlock.SHADOWS, card.getBlueprint().getSiteBlock());
		assertEquals(2, card.getBlueprint().getTwilightCost());
		assertEquals(LotroCardBlueprint.Direction.LEFT, card.getBlueprint().getSiteDirection());
		assertTrue(scn.HasKeyword(card, Keyword.RIVER));
	}

	@Test
	public void EachCharacterIsStrengthPlusOneForEachCardHeBears() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetShadowsScenario();

		var frodo = scn.GetRingBearer();
		var aragorn = scn.GetFreepsCard("aragorn");
		var nostranger = scn.GetFreepsCard("nostranger");
		var worker = scn.GetFreepsCard("worker");
		var sam = scn.GetFreepsCard("sam");
		var gimli = scn.GetFreepsCard("gimli");
		var armor = scn.GetFreepsCard("armor");
		var maggot = scn.GetFreepsCard("maggot");
		var pipe = scn.GetFreepsCard("pipe");
		scn.MoveCompanionsToTable(aragorn, sam, gimli);
		scn.MoveAlliesToTable(maggot);
		scn.AttachCardsTo(aragorn, nostranger, worker);
		scn.AttachCardsTo(maggot, pipe);
		scn.MoveCardsToHand(armor);

		var poison = scn.GetShadowCard("poison");
		var troop = scn.GetShadowCard("troop");
		var scimitar = scn.GetShadowCard("scimitar");
		scn.AttachCardsTo(aragorn, poison);

		scn.StartGame(scn.GetFreepsSite("site1"));
		assertEquals("Fords of Isen", scn.GetCurrentSite().getBlueprint().getTitle());
		// A minion laid early, to read the standing text on him beside the companions (Q-0228).
		scn.MoveMinionsToTable(troop);
		scn.AttachCardsTo(troop, scimitar);

		// A Free Peoples condition, a follower and a Shadow possession: 8 + 3
		assertEquals(11, scn.GetStrength(aragorn));
		// The One Ring is borne by the Ring-bearer: 3 + 1 for The Ruling Ring + 1
		assertEquals(5, scn.GetStrength(frodo));
		// An ally bearing a pipe: 2 + 1
		assertEquals(3, scn.GetStrength(maggot));
		// A minion bearing a weapon: 13 + 2 for the scimitar + 1
		assertEquals(16, scn.GetStrength(troop));
		// Nothing borne, whatever the others bear.
		assertEquals(3, scn.GetStrength(sam));
		assertEquals(6, scn.GetStrength(gimli));

		scn.FreepsPlayCard(armor);
		assertAttachedTo(armor, gimli);
		// The armor changes no strength of its own: 6 + 1
		assertEquals(7, scn.GetStrength(gimli));
		assertEquals(3, scn.GetStrength(sam));
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void OnceTheFellowshipHasMovedOnFromTheFordsTheBonusIsGone() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetShadowsScenario();

		var frodo = scn.GetRingBearer();
		var aragorn = scn.GetFreepsCard("aragorn");
		var nostranger = scn.GetFreepsCard("nostranger");
		var worker = scn.GetFreepsCard("worker");
		var maggot = scn.GetFreepsCard("maggot");
		var pipe = scn.GetFreepsCard("pipe");
		scn.MoveCompanionsToTable(aragorn);
		scn.MoveAlliesToTable(maggot);
		scn.AttachCardsTo(aragorn, nostranger, worker);
		scn.AttachCardsTo(maggot, pipe);

		var troop = scn.GetShadowCard("troop");
		var scimitar = scn.GetShadowCard("scimitar");

		scn.StartGame(scn.GetFreepsSite("site1"));
		scn.MoveMinionsToTable(troop);
		scn.AttachCardsTo(troop, scimitar);

		// 8 + 2; 3 + 1 + 1; 2 + 1; 13 + 2 + 1
		assertEquals(10, scn.GetStrength(aragorn));
		assertEquals(5, scn.GetStrength(frodo));
		assertEquals(3, scn.GetStrength(maggot));
		assertEquals(16, scn.GetStrength(troop));

		scn.FreepsPass();
		scn.ShadowChooseCardBPFromSelection(scn.GetShadowSite("site2"));
		assertEquals("Sirannon Ruins", scn.GetCurrentSite().getBlueprint().getTitle());
		assertTrue(scn.AwaitingShadowPhaseActions());

		assertAttachedTo(nostranger, aragorn);
		assertAttachedTo(worker, aragorn);
		assertAttachedTo(pipe, maggot);
		assertAttachedTo(scimitar, troop);
		assertEquals(8, scn.GetStrength(aragorn));
		// 3 + 1 for The Ruling Ring
		assertEquals(4, scn.GetStrength(frodo));
		assertEquals(2, scn.GetStrength(maggot));
		// 13 + 2 for the scimitar
		assertEquals(15, scn.GetStrength(troop));
	}

	@Test
	public void AHinderedCardACharacterBearsStrengthensHimLikeTheOthers() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetShadowsScenario();

		var aragorn = scn.GetFreepsCard("aragorn");
		var nostranger = scn.GetFreepsCard("nostranger");
		var worker = scn.GetFreepsCard("worker");
		scn.MoveCompanionsToTable(aragorn);

		scn.StartGame(scn.GetFreepsSite("site1"));
		assertEquals("Fords of Isen", scn.GetCurrentSite().getBlueprint().getTitle());

		assertEquals(8, scn.GetStrength(aragorn));
		scn.AttachCardsTo(aragorn, nostranger, worker);
		// 8 + 1 for each of two cards, neither of which changes his strength
		assertEquals(10, scn.GetStrength(aragorn));

		// A hindered card is a card still, and still borne
		scn.HinderCard(nostranger);
		assertTrue(scn.IsHindered(nostranger));
		assertEquals(10, scn.GetStrength(aragorn));
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}
}

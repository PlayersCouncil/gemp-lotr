package com.gempukku.lotro.cards.official.set08;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.game.PhysicalCardImpl;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static org.junit.Assert.*;

public class Card_08_008_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("wish", "8_8");
					put("wish2", "8_8");        // a second copy, to show which copy "here" is
					put("gimli", "1_13");       // Gimli, Son of Gloin: [dwarven] Dwarf companion, vitality 3, damage +1; a skirmish ability used only on purpose
					put("thrarin", "1_27");     // Thrarin, Dwarven Smith: [dwarven] Dwarf ally, vitality 2; a maneuver ability used only on purpose
					put("legolas", "1_50");     // Legolas, Greenleaf: [elven] Elf companion, vitality 3; an archery ability used only on purpose
					put("arwen", "1_30");       // Arwen, Daughter of Elrond: [elven] Elf companion; stronger only against a Nazgul
					put("orophin", "1_56");     // Orophin, Lorien Bowman: [elven] Elf ally, vitality 3; an archery ability used only on purpose
					put("aragorn", "1_89");     // Aragorn, Ranger of the North: [gondor] Man companion, vitality 4; a maneuver ability used only on purpose
					put("merchant", "7_97");    // Gondorian Merchant: [gondor] Man ally, vitality 1; a maneuver ability used only on purpose
					put("narsil", "9_34");      // Narsil, Blade of the Faithful: vitality +1 to a [gondor] Man, so that the Merchant can exert
					put("pippin", "103_31");    // Pippin, Trollslayer: a [gondor] companion who is a Hobbit, no Man; a skirmish ability used only on purpose
					put("eomer", "4_267");      // Eomer, Third Marshal of Riddermark: a Man companion of [rohan]; his strength text reads wounds on minions he skirmishes

					put("raiders", "1_158");    // Uruk-hai Raiding Party: strength 9, damage +1, no other text
					put("troop", "1_177");      // Goblin Patrol Troop: strength 13, no text
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	protected VirtualTableScenario GetDwarvesScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("wish", "8_8");
					// Thrarin, Dwarven Smith: Dwarf ally, vitality 2; his one ability is a maneuver ability
					put("thrarin", "1_27");
					put("gimli", "1_13");       // Gimli, Son of Gloin: Dwarf companion, vitality 3; a skirmish ability that is never used
					put("guard", "1_7");        // Dwarf Guard: Dwarf companion, vitality 2; no text once in play
					put("legolas", "1_50");     // Legolas, Greenleaf: [elven] companion, vitality 3; an archery ability that is never used
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void WishForOurKinfolkStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 8
		 * Name: Wish For Our Kinfolk
		 * Unique: False
		 * Side: Free Peoples
		 * Culture: Dwarven
		 * Twilight Cost: 1
		 * Type: Condition
		 * Subtype: Support Area
		 * Game Text: <b>Fellowship:</b> Exert a Dwarf companion and either an [elven] companion or a [gondor] Man to add a [dwarven] token here.<br><b>Skirmish:</b> Remove a [dwarven] token here to make a companion <b>damage +1</b>.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("wish");

		assertEquals("Wish For Our Kinfolk", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.FREE_PEOPLE, card.getBlueprint().getSide());
		assertEquals(Culture.DWARVEN, card.getBlueprint().getCulture());
		assertEquals(CardType.CONDITION, card.getBlueprint().getCardType());
		assertTrue(scn.HasKeyword(card, Keyword.SUPPORT_AREA));
		assertEquals(1, card.getBlueprint().getTwilightCost());
	}

	@Test
	public void FellowshipActionIsPaidByADwarfCompanionAndNeverByADwarfAlly() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetDwarvesScenario();

		var wish = scn.GetFreepsCard("wish");
		var thrarin = scn.GetFreepsCard("thrarin");
		var gimli = scn.GetFreepsCard("gimli");
		var guard = scn.GetFreepsCard("guard");
		var legolas = scn.GetFreepsCard("legolas");
		scn.MoveCardsToSupportArea(wish);
		scn.MoveAlliesToTable(thrarin);
		scn.MoveCompanionsToTable(legolas);

		scn.StartGame();

		// Thrarin is a Dwarf who can exert and Legolas an [elven] companion who can: the one thing missing from
		// "exert a Dwarf companion" is that the Dwarf be a companion.
		assertEquals(0, scn.GetWoundsOn(thrarin));
		assertEquals(0, scn.GetWoundsOn(legolas));
		assertFalse(scn.FreepsActionAvailable(wish));
		assertTrue(scn.AwaitingFellowshipPhaseActions());

		scn.MoveCompanionsToTable(gimli, guard);
		scn.FreepsRefreshDecision();

		assertEquals(0, scn.GetCultureTokensOn(wish));
		assertTrue(scn.FreepsActionAvailable(wish));
		scn.FreepsUseCardAction(wish);

		assertTrue(scn.FreepsHasCardChoiceAvailable(gimli, guard));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(thrarin));
		assertEquals(1, scn.FreepsGetChoiceMin());
		scn.FreepsChooseCard(guard);

		// Legolas is the only [elven] companion and there is no [gondor] Man, so he exerts with nothing asked.
		assertEquals(1, scn.GetWoundsOn(guard));
		assertEquals(0, scn.GetWoundsOn(gimli));
		assertEquals(0, scn.GetWoundsOn(thrarin));
		assertEquals(1, scn.GetWoundsOn(legolas));
		assertEquals(1, scn.GetCultureTokensOn(wish));
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void FellowshipActionExertsADwarfAndAnElvenCompanionOrAGondorManToAddATokenToThisCopy() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var wish = scn.GetFreepsCard("wish");
		var wish2 = scn.GetFreepsCard("wish2");
		var gimli = scn.GetFreepsCard("gimli");
		var legolas = scn.GetFreepsCard("legolas");
		var arwen = scn.GetFreepsCard("arwen");
		var orophin = scn.GetFreepsCard("orophin");
		var aragorn = scn.GetFreepsCard("aragorn");
		var merchant = scn.GetFreepsCard("merchant");
		var narsil = scn.GetFreepsCard("narsil");
		var pippin = scn.GetFreepsCard("pippin");
		var eomer = scn.GetFreepsCard("eomer");
		var frodo = scn.GetRingBearer();
		scn.MoveCardsToSupportArea(wish, wish2);
		scn.MoveCompanionsToTable(gimli, legolas, arwen, aragorn, pippin, eomer);
		scn.MoveAlliesToTable(orophin, merchant);
		scn.AttachCardsTo(merchant, narsil);

		scn.StartGame();

		assertEquals(0, scn.GetCultureTokensOn(wish));
		assertEquals(0, scn.GetCultureTokensOn(wish2));
		assertEquals(0, scn.GetWoundsOn(gimli));
		assertEquals(0, scn.GetWoundsOn(merchant));
		assertEquals(2, scn.GetVitality(merchant));
		assertTrue(scn.FreepsActionAvailable(wish));
		scn.FreepsUseCardAction(wish);

		// Gimli is the only Dwarf companion and exerts unasked. Then the second half: [elven] companions and
		// [gondor] Men of either kind, both alternatives on offer at once; not an [elven] ally, a [gondor]
		// companion who is no Man, a Man of another culture, nor Gimli or Frodo.
		assertEquals(1, scn.GetWoundsOn(gimli));
		assertTrue(scn.FreepsHasCardChoiceAvailable(legolas, arwen, aragorn, merchant));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(orophin, pippin, eomer, gimli, frodo));
		assertEquals(1, scn.FreepsGetChoiceMin());
		scn.FreepsChooseCard(merchant);

		assertEquals(1, scn.GetWoundsOn(merchant));
		assertEquals(0, scn.GetWoundsOn(legolas));
		assertEquals(0, scn.GetWoundsOn(arwen));
		assertEquals(0, scn.GetWoundsOn(aragorn));
		assertEquals(1, scn.GetCultureTokensOn(wish));
		assertEquals(0, scn.GetCultureTokensOn(wish2));
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void WithNoGondorManAbleToExertAnElvenCompanionPaysAndThenTheActionIsNotOffered() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var wish = scn.GetFreepsCard("wish");
		var gimli = scn.GetFreepsCard("gimli");
		var legolas = scn.GetFreepsCard("legolas");
		var orophin = scn.GetFreepsCard("orophin");
		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCardsToSupportArea(wish);
		scn.MoveCompanionsToTable(gimli, legolas, aragorn);
		scn.MoveAlliesToTable(orophin);

		scn.StartGame();
		// Aragorn, vitality 4, exhausted; Legolas, vitality 3, can exert once more.
		scn.AddWoundsToChar(aragorn, 3);
		scn.AddWoundsToChar(legolas, 1);
		scn.FreepsRefreshDecision();

		assertEquals(3, scn.GetWoundsOn(aragorn));
		assertEquals(1, scn.GetWoundsOn(legolas));
		assertEquals(0, scn.GetWoundsOn(gimli));
		assertEquals(0, scn.GetWoundsOn(orophin));
		assertEquals(0, scn.GetCultureTokensOn(wish));
		assertTrue(scn.FreepsActionAvailable(wish));
		scn.FreepsUseCardAction(wish);

		// The [elven] alternative is the only one that can be paid, and is.
		assertEquals(1, scn.GetWoundsOn(gimli));
		assertEquals(2, scn.GetWoundsOn(legolas));
		assertEquals(3, scn.GetWoundsOn(aragorn));
		assertEquals(1, scn.GetCultureTokensOn(wish));

		// Now neither an [elven] companion nor a [gondor] Man can exert, though Gimli can and so can Orophin,
		// an [elven] ally.
		assertFalse(scn.FreepsActionAvailable(wish));
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void WithNoElvenCompanionAbleToExertAGondorManPaysAndAnExhaustedDwarfCompanionStopsTheAction() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var wish = scn.GetFreepsCard("wish");
		var gimli = scn.GetFreepsCard("gimli");
		var thrarin = scn.GetFreepsCard("thrarin");
		var legolas = scn.GetFreepsCard("legolas");
		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCardsToSupportArea(wish);
		scn.MoveCompanionsToTable(gimli, legolas, aragorn);
		scn.MoveAlliesToTable(thrarin);

		scn.StartGame();
		// Legolas, vitality 3, exhausted; Gimli, vitality 3, can exert once more.
		scn.AddWoundsToChar(legolas, 2);
		scn.AddWoundsToChar(gimli, 1);
		scn.FreepsRefreshDecision();

		assertEquals(2, scn.GetWoundsOn(legolas));
		assertEquals(1, scn.GetWoundsOn(gimli));
		assertEquals(0, scn.GetWoundsOn(aragorn));
		assertEquals(0, scn.GetWoundsOn(thrarin));
		assertEquals(0, scn.GetCultureTokensOn(wish));
		assertTrue(scn.FreepsActionAvailable(wish));
		scn.FreepsUseCardAction(wish);

		// The [gondor] alternative is the only one that can be paid, and is.
		assertEquals(2, scn.GetWoundsOn(gimli));
		assertEquals(1, scn.GetWoundsOn(aragorn));
		assertEquals(2, scn.GetWoundsOn(legolas));
		assertEquals(1, scn.GetCultureTokensOn(wish));

		// Gimli is now exhausted: Aragorn can still exert, and so can Thrarin, a Dwarf who is no companion.
		assertFalse(scn.FreepsActionAvailable(wish));
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void SkirmishActionRemovesATokenFromThisCopyToMakeACompanionDamagePlus1ForThatSkirmish() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var wish = scn.GetFreepsCard("wish");
		var wish2 = scn.GetFreepsCard("wish2");
		var gimli = scn.GetFreepsCard("gimli");
		var aragorn = scn.GetFreepsCard("aragorn");
		var thrarin = scn.GetFreepsCard("thrarin");
		var frodo = scn.GetRingBearer();
		scn.MoveCardsToSupportArea(wish, wish2);
		scn.MoveCompanionsToTable(gimli, aragorn);
		scn.MoveAlliesToTable(thrarin);

		var raiders = scn.GetShadowCard("raiders");
		var troop = scn.GetShadowCard("troop");

		scn.StartGame();
		// Each copy has gained one token by its fellowship ability in an earlier turn.
		scn.AddTokensToCard(wish, 1);
		scn.AddTokensToCard(wish2, 1);
		assertEquals(1, scn.GetCultureTokensOn(wish));
		assertEquals(1, scn.GetCultureTokensOn(wish2));
		scn.MoveMinionsToTable(raiders, troop);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(
				new PhysicalCardImpl[]{gimli, raiders},
				new PhysicalCardImpl[]{aragorn, troop});

		scn.FreepsResolveSkirmish(gimli);
		assertEquals(0, scn.GetKeywordCount(aragorn, Keyword.DAMAGE));
		assertEquals(0, scn.GetKeywordCount(frodo, Keyword.DAMAGE));
		assertTrue(scn.FreepsActionAvailable(wish));
		scn.FreepsUseCardAction(wish);

		// Any companion: the one skirmishing, one assigned to a later skirmish, one not assigned at all. Not an
		// ally, nor a minion.
		assertTrue(scn.FreepsHasCardChoiceAvailable(gimli, aragorn, frodo));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(thrarin, raiders, troop));
		assertEquals(1, scn.FreepsGetChoiceMin());
		scn.FreepsChooseCard(aragorn);

		assertEquals(0, scn.GetCultureTokensOn(wish));
		assertEquals(1, scn.GetCultureTokensOn(wish2));
		assertEquals(1, scn.GetKeywordCount(aragorn, Keyword.DAMAGE));
		assertEquals(0, scn.GetKeywordCount(frodo, Keyword.DAMAGE));
		// Gimli's own printed damage +1, untouched.
		assertEquals(1, scn.GetKeywordCount(gimli, Keyword.DAMAGE));

		scn.ShadowPass();
		// Later in the same skirmish: still damage +1. This copy has no token left and is not offered; the
		// second copy, which has one, is.
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
		assertEquals(1, scn.GetKeywordCount(aragorn, Keyword.DAMAGE));
		assertFalse(scn.FreepsActionAvailable(wish));
		assertTrue(scn.FreepsActionAvailable(wish2));

		scn.FreepsPass();
		// Gimli, 6 against 9, loses: a wound and the Raiding Party's damage +1, of his 3 vitality.
		assertEquals(2, scn.GetWoundsOn(gimli));

		// The next skirmish is a phase of its own: the bonus is gone.
		scn.FreepsResolveSkirmish(aragorn);
		assertTrue(scn.IsCharSkirmishingAgainst(aragorn, troop));
		assertEquals(0, scn.GetKeywordCount(aragorn, Keyword.DAMAGE));
		assertFalse(scn.FreepsActionAvailable(wish));
		assertTrue(scn.FreepsActionAvailable(wish2));
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
	}
}

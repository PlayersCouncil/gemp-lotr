package com.gempukku.lotro.cards.official.set01;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_01_062_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("banners", "1_62");
					put("legolas", "1_51");     // Legolas, Prince of Mirkwood: Elf companion, archer; strength +3 only against a Nazgul
					put("arwen", "1_30");       // Arwen, Daughter of Elrond: Elf companion who is no archer; strength +3 only against a Nazgul
					put("orophin", "1_56");     // Orophin, Lorien Bowman: Elf ally, archer; his archery ability is used only on purpose
					put("aragorn", "1_89");     // Aragorn, Ranger of the North: a companion who is no Elf; maneuver ability only
					put("troop", "1_177");      // Goblin Patrol Troop: a minion with no text, vitality 3
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void TheSplendorOfTheirBannersStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 1
		 * Name: The Splendor of Their Banners
		 * Unique: True
		 * Side: Free Peoples
		 * Culture: Elven
		 * Twilight Cost: 1
		 * Type: Condition
		 * Subtype:
		 * Game Text: <b>Tale</b>. Bearer must be an Elf companion.
		 * <b>Archery:</b> If bearer is an archer, exert bearer to make the fellowship archery total +1.
		 */

		var scn = GetScenario();

		var card = scn.GetFreepsCard("banners");

		assertEquals("The Splendor of Their Banners", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertTrue(card.getBlueprint().isUnique());
		assertEquals(Side.FREE_PEOPLE, card.getBlueprint().getSide());
		assertEquals(Culture.ELVEN, card.getBlueprint().getCulture());
		assertEquals(CardType.CONDITION, card.getBlueprint().getCardType());
		assertTrue(scn.HasKeyword(card, Keyword.TALE));
		assertEquals(1, card.getBlueprint().getTwilightCost());
	}

	@Test
	public void CanBePlayedOnAnElfCompanionAndOnNoOtherCharacter() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var banners = scn.GetFreepsCard("banners");
		var legolas = scn.GetFreepsCard("legolas");
		var arwen = scn.GetFreepsCard("arwen");
		var orophin = scn.GetFreepsCard("orophin");
		var aragorn = scn.GetFreepsCard("aragorn");
		var frodo = scn.GetRingBearer();
		scn.MoveCompanionsToTable(legolas, arwen, aragorn);
		scn.MoveAlliesToTable(orophin);
		scn.MoveCardsToHand(banners);

		scn.StartGame();

		assertTrue(scn.FreepsPlayAvailable(banners));
		scn.FreepsPlayCard(banners);
		// Two Elf companions, an archer and one who is not; not the Elf ally, nor the Man or the Hobbit companion.
		assertTrue(scn.FreepsHasCardChoiceAvailable(legolas, arwen));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(orophin, aragorn, frodo));
		assertEquals(1, scn.FreepsGetChoiceMin());
		scn.FreepsChooseCard(arwen);

		assertAttachedTo(banners, arwen);
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void ArcheryAbilityExertsAnArcherBearerToMakeTheFellowshipArcheryTotalOneMoreForTheArcheryPhase() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var banners = scn.GetFreepsCard("banners");
		var legolas = scn.GetFreepsCard("legolas");
		var arwen = scn.GetFreepsCard("arwen");
		var troop = scn.GetShadowCard("troop");
		scn.MoveCompanionsToTable(legolas, arwen);
		scn.AttachCardsTo(legolas, banners);

		scn.StartGame();
		scn.MoveMinionsToTable(troop);

		// The phase before: not offered.
		scn.SkipToPhase(Phase.MANEUVER);
		assertTrue(scn.AwaitingFreepsManeuverPhaseActions());
		assertFalse(scn.FreepsActionAvailable(banners));
		scn.SkipToPhase(Phase.ARCHERY);

		assertTrue(scn.AwaitingFreepsArcheryPhaseActions());
		assertTrue(scn.FreepsActionAvailable(banners));
		// Legolas, the only archer, adds his 1.
		assertEquals(1, scn.GetFreepsArcheryTotal());
		assertEquals(0, scn.GetShadowArcheryTotal());
		assertEquals(0, scn.GetWoundsOn(legolas));
		assertEquals(0, scn.GetWoundsOn(arwen));

		scn.FreepsUseCardAction(banners);
		// The bearer exerts, and nobody else.
		assertEquals(1, scn.GetWoundsOn(legolas));
		assertEquals(0, scn.GetWoundsOn(arwen));
		assertEquals(2, scn.GetFreepsArcheryTotal());
		assertEquals(0, scn.GetShadowArcheryTotal());
		assertEquals(0, scn.GetWoundsOn(troop));

		scn.ShadowPass();
		scn.FreepsPass();
		scn.ShadowPass();

		// Two archery wounds go to the only minion; the +1 lasted the archery phase and no longer.
		assertEquals(2, scn.GetWoundsOn(troop));
		assertTrue(scn.AwaitingFreepsAssignmentPhaseActions());
		assertEquals(1, scn.GetFreepsArcheryTotal());
	}

	@Test
	public void NotOfferedWhileTheBearerIsNoArcherThoughAnotherCompanionIs() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var banners = scn.GetFreepsCard("banners");
		var legolas = scn.GetFreepsCard("legolas");
		var arwen = scn.GetFreepsCard("arwen");
		var troop = scn.GetShadowCard("troop");
		scn.MoveCompanionsToTable(legolas, arwen);
		scn.AttachCardsTo(arwen, banners);

		scn.StartGame();
		scn.MoveMinionsToTable(troop);
		scn.SkipToPhase(Phase.ARCHERY);

		// Arwen bears it and is no archer; Legolas is an archer and does not bear it.
		assertAttachedTo(banners, arwen);
		assertEquals(1, scn.GetFreepsArcheryTotal());
		assertTrue(scn.AwaitingFreepsArcheryPhaseActions());
		assertFalse(scn.FreepsActionAvailable(banners));
	}

	@Test
	public void NotOfferedWhileTheArcherBearerIsExhausted() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var banners = scn.GetFreepsCard("banners");
		var legolas = scn.GetFreepsCard("legolas");
		var arwen = scn.GetFreepsCard("arwen");
		var troop = scn.GetShadowCard("troop");
		scn.MoveCompanionsToTable(legolas, arwen);
		scn.AttachCardsTo(legolas, banners);
		scn.AddWoundsToChar(legolas, 2);

		scn.StartGame();
		scn.MoveMinionsToTable(troop);
		scn.SkipToPhase(Phase.ARCHERY);

		// Legolas, vitality 3 with 2 wounds, cannot exert; Arwen beside him could, but does not bear it.
		assertEquals(2, scn.GetWoundsOn(legolas));
		assertTrue(scn.IsExhausted(legolas));
		assertTrue(scn.AwaitingFreepsArcheryPhaseActions());
		assertFalse(scn.FreepsActionAvailable(banners));
	}
}

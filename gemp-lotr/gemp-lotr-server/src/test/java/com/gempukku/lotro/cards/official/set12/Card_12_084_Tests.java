package com.gempukku.lotro.cards.official.set12;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_12_084_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("field", "12_84");
					put("prowler", "11_136");   // Prowling Orc: [orc] Orc, lurker; a skirmish ability that removes (2), used only on purpose
					put("halftroll", "14_12");  // Half-troll of Far Harad: [orc] Half-troll, no lurker; its trigger is at the start of an assignment phase
					put("gauntlets", "11_109"); // Bladed Gauntlets: on an [orc] minion, "Bearer is a lurker"; a skirmish ability used only on purpose
					put("column", "11_72");     // Column of Easterlings: [men] Man, lurker; a skirmish ability used only on purpose
					put("denizen", "11_115");   // Denizen of Khazad-dûm: [orc] Orc, no lurker; +2 only at an underground site

					put("arwen", "1_30");       // Arwen, Daughter of Elrond: Elf companion, vitality 3; +3 only against a Nazgûl
					put("celeborn", "1_34");    // Celeborn, Lord of Lórien: Elf ally, vitality 3; a fellowship ability used only on purpose
					put("gimli", "1_13");       // Gimli, Son of Glóin: a Dwarf companion, vitality 3, damage +1
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void BloodstainedFieldStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 12
		 * Name: Bloodstained Field
		 * Unique: False
		 * Side: Shadow
		 * Culture: Orc
		 * Twilight Cost: 1
		 * Type: Event
		 * Subtype: Archery
		 * Game Text: Spot an [orc] lurker to wound an Elf.
		*/

		var scn = GetScenario();

		var field = scn.GetFreepsCard("field");

		assertEquals("Bloodstained Field", field.getBlueprint().getTitle());
		assertNull(field.getBlueprint().getSubtitle());
		assertFalse(field.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, field.getBlueprint().getSide());
		assertEquals(Culture.ORC, field.getBlueprint().getCulture());
		assertEquals(CardType.EVENT, field.getBlueprint().getCardType());
		assertTrue(scn.HasTimeword(field, Timeword.ARCHERY));
		assertEquals(1, field.getBlueprint().getTwilightCost());
	}

	@Test
	public void SpottingAnOrcLurkerWoundsAnElfCompanionOrAllyAndNoOther() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var field = scn.GetShadowCard("field");
		var prowler = scn.GetShadowCard("prowler");
		var arwen = scn.GetFreepsCard("arwen");
		var celeborn = scn.GetFreepsCard("celeborn");
		var gimli = scn.GetFreepsCard("gimli");
		var frodo = scn.GetRingBearer();
		scn.MoveCompanionsToTable(arwen, gimli);
		scn.MoveAlliesToTable(celeborn);

		scn.StartGame();
		scn.SetShadowHand(field);
		scn.MoveMinionsToTable(prowler);
		// Vitality 3 with 2 wounds: Celeborn is exhausted, so a wound kills him where an exertion could not be paid.
		scn.AddWoundsToChar(celeborn, 2);
		assertEquals(2, scn.GetWoundsOn(celeborn));
		assertInHand(field);

		scn.SkipToPhase(Phase.ARCHERY);
		scn.FreepsPass();
		scn.SetTwilight(5);
		scn.ShadowRefreshDecision();
		assertTrue(scn.AwaitingShadowArcheryPhaseActions());
		assertTrue(scn.ShadowPlayAvailable(field));
		assertEquals(0, scn.GetWoundsOn(arwen));

		scn.ShadowPlayCard(field);
		// The Elf companion and the exhausted Elf ally are offered; Gimli and Frodo are not.
		assertTrue(scn.ShadowHasCardChoiceAvailable(arwen, celeborn));
		assertTrue(scn.ShadowHasCardChoiceNotAvailable(gimli, frodo));
		assertEquals(1, scn.ShadowGetChoiceMin());
		scn.ShadowChooseCard(celeborn);

		assertInZone(Zone.DEAD, celeborn);
		assertEquals(0, scn.GetWoundsOn(arwen));
		assertEquals(0, scn.GetWoundsOn(gimli));
		assertEquals(0, scn.GetWoundsOn(frodo));
		assertInDiscard(field);
		assertTrue(scn.AwaitingFreepsArcheryPhaseActions());
	}

	@Test
	public void AnOrcMinionMadeALurkerByAnotherCardIsSpottedToo() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var field = scn.GetShadowCard("field");
		var halftroll = scn.GetShadowCard("halftroll");
		var gauntlets = scn.GetShadowCard("gauntlets");
		var arwen = scn.GetFreepsCard("arwen");
		scn.MoveCompanionsToTable(arwen);

		scn.StartGame();
		scn.SetShadowHand(field);
		scn.MoveMinionsToTable(halftroll);
		scn.AttachCardsTo(halftroll, gauntlets);
		assertEquals(halftroll, gauntlets.getAttachedTo());
		assertInHand(field);

		scn.SkipToPhase(Phase.ARCHERY);
		scn.FreepsPass();
		scn.SetTwilight(5);
		scn.ShadowRefreshDecision();
		// An [orc] Half-troll, a lurker only by the Gauntlets it bears, is the one [orc] lurker in play.
		assertTrue(scn.HasKeyword(halftroll, Keyword.LURKER));
		assertTrue(scn.ShadowPlayAvailable(field));
		assertEquals(0, scn.GetWoundsOn(arwen));

		scn.ShadowPlayCard(field);
		assertEquals(1, scn.GetWoundsOn(arwen));
		assertInDiscard(field);
		assertTrue(scn.AwaitingFreepsArcheryPhaseActions());
	}

	@Test
	public void CannotBePlayedWithALurkerOfAnotherCultureAndOrcMinionsWhoAreNoLurkers() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var field = scn.GetShadowCard("field");
		var column = scn.GetShadowCard("column");
		var denizen = scn.GetShadowCard("denizen");
		var halftroll = scn.GetShadowCard("halftroll");
		var arwen = scn.GetFreepsCard("arwen");
		scn.MoveCompanionsToTable(arwen);

		scn.StartGame();
		scn.SetShadowHand(field);
		scn.MoveMinionsToTable(column, denizen, halftroll);
		assertInHand(field);

		scn.SkipToPhase(Phase.ARCHERY);
		scn.FreepsPass();
		scn.SetTwilight(5);
		scn.ShadowRefreshDecision();
		assertTrue(scn.AwaitingShadowArcheryPhaseActions());
		assertTrue(scn.HasKeyword(column, Keyword.LURKER));
		assertFalse(scn.ShadowPlayAvailable(field));
		assertInHand(field);
	}

	@Test
	public void WithNoElfInPlayItIsPlayedAndWoundsNobody() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var field = scn.GetShadowCard("field");
		var prowler = scn.GetShadowCard("prowler");
		var gimli = scn.GetFreepsCard("gimli");
		var frodo = scn.GetRingBearer();
		scn.MoveCompanionsToTable(gimli);

		scn.StartGame();
		scn.SetShadowHand(field);
		scn.MoveMinionsToTable(prowler);
		assertInHand(field);

		scn.SkipToPhase(Phase.ARCHERY);
		scn.FreepsPass();
		scn.SetTwilight(5);
		scn.ShadowRefreshDecision();
		assertTrue(scn.ShadowPlayAvailable(field));

		scn.ShadowPlayCard(field);
		assertEquals(0, scn.GetWoundsOn(gimli));
		assertEquals(0, scn.GetWoundsOn(frodo));
		assertEquals(0, scn.GetWoundsOn(prowler));
		assertInDiscard(field);
		assertTrue(scn.AwaitingFreepsArcheryPhaseActions());
	}
}
